package io.unthrottled.theme.randomizer.services

import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.Alarm
import io.unthrottled.theme.randomizer.config.ChangeIntervals
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.theme.randomizer.listeners.ThemeChangedListener
import io.unthrottled.theme.randomizer.tools.runSafelyWithResult
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class ThemeChangeEventEmitter : Runnable, Disposable {
  private val messageBus = ApplicationManager.getApplication().messageBus.connect()
  private val log = Logger.getInstance(this::class.java)
  private val themeChangeAlarm = Alarm()

  init {
    val self = this
    messageBus.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        themeChangeAlarm.cancelAllRequests()
        if (newPluginState.isChangeTheme) {
          themeChangeAlarm.addRequest(
            self,
            TimeUnit.MILLISECONDS.convert(
              getThemeChangeInterval(newPluginState),
              TimeUnit.MINUTES
            ).toInt()
          )
        }
      }
    )
    themeChangeAlarm.addRequest(
      this,
      TimeUnit.MILLISECONDS.convert(
        getDuration(),
        TimeUnit.MINUTES
      ).toInt()
    )
  }

  private fun getDuration(): Long =
    if (Config.instance.lastChangeTime > -1) {
      runSafelyWithResult({
        maxOf(
          getIdleTimeInMinutes() - Duration.between(
            Instant.ofEpochSecond(Config.instance.lastChangeTime),
            Instant.now()
          ).toMinutes(),
          0
        )
      }) { getIdleTimeInMinutes() }
    } else {
      getIdleTimeInMinutes()
    }

  private fun scheduleThemeChange() {
    themeChangeAlarm.addRequest(
      this,
      TimeUnit.MILLISECONDS.convert(
        getIdleTimeInMinutes(),
        TimeUnit.MINUTES
      ).toInt()
    )
  }

  private fun getIdleTimeInMinutes(): Long =
    getThemeChangeInterval(Config.instance)

  @SuppressWarnings("MagicNumber")
  private fun getThemeChangeInterval(newPluginState: Config): Long =
    ChangeIntervals.getValue(newPluginState.interval)
      .map {
        when (it) {
          ChangeIntervals.FIVE_MINUTES -> 5L
          ChangeIntervals.FIFTEEN_MINUTES -> 15L
          ChangeIntervals.THIRTY_MINUTES -> 30L
          ChangeIntervals.HOUR -> 60L
          ChangeIntervals.DAY -> 1440L
        }
      }.orElse(60L)

  override fun dispose() {
    messageBus.dispose()
    themeChangeAlarm.dispose()
  }

  override fun run() {
    if (Config.instance.isChangeTheme.not()) return

    val nextTheme = if (Config.instance.isRandomOrder) {
      ThemeService.instance.getRandomTheme()
    } else ThemeService.instance.getNextTheme()
    nextTheme.ifPresent {
      QuickChangeLookAndFeel.switchLafAndUpdateUI(
        LafManager.getInstance(),
        it,
        true
      )
      Config.instance.lastChangeTime = Instant.now().epochSecond
      ApplicationManager.getApplication().messageBus
        .syncPublisher(ThemeChangedListener.TOPIC)
    }
    scheduleThemeChange()
  }
}
