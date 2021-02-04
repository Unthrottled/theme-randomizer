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
              getThemeSwitchCheckIntervalFromState(newPluginState),
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
      getThemeSwitchCheckInterval()
    } else {
      captureTimestamp()
      getThemeSwitchCheckInterval()
    }

  private fun scheduleThemeChange() {
    themeChangeAlarm.addRequest(
      this,
      TimeUnit.MILLISECONDS.convert(
        getThemeSwitchCheckInterval(),
        TimeUnit.MINUTES
      ).toInt()
    )
  }

  private fun getThemeSwitchCheckInterval(): Long =
    getThemeSwitchCheckIntervalFromState(Config.instance)

  @SuppressWarnings("MagicNumber")
  private fun getThemeSwitchCheckIntervalFromState(newPluginState: Config): Long =
    ChangeIntervals.getValue(newPluginState.interval)
      .map {
        when (it) {
          ChangeIntervals.THIRTY_MINUTES -> 30L
          ChangeIntervals.WEEK,
          ChangeIntervals.TWO_DAYS,
          ChangeIntervals.DAY,
          ChangeIntervals.HOUR -> 60L
        }
      }.orElse(60L)

  override fun dispose() {
    messageBus.dispose()
    themeChangeAlarm.dispose()
  }

  override fun run() {
    if (Config.instance.isChangeTheme.not() || isTime().not()) return

    val nextTheme = if (Config.instance.isRandomOrder) {
      ThemeService.instance.getRandomTheme()
    } else ThemeService.instance.getNextTheme()
    nextTheme.ifPresent {
      QuickChangeLookAndFeel.switchLafAndUpdateUI(
        LafManager.getInstance(),
        it,
        true
      )
      captureTimestamp() // todo: reset timestamp on manual laf setting
      ApplicationManager.getApplication().messageBus
        .syncPublisher(ThemeChangedListener.TOPIC)
    }
    scheduleThemeChange()
  }

  private fun isTime(): Boolean {
    return getThemeChangeIntervalInMinutes() <= Duration.between(
      Instant.ofEpochSecond(Config.instance.lastChangeTime),
      Instant.now()
    ).toMinutes()
  }

  @SuppressWarnings("MagicNumber")
  private fun getThemeChangeIntervalInMinutes(): Long {
    return ChangeIntervals.getValue(Config.instance.interval)
      .map {
        when (it) {
          ChangeIntervals.THIRTY_MINUTES -> 30L
          ChangeIntervals.HOUR -> 60L
          ChangeIntervals.DAY -> 1440L
          ChangeIntervals.TWO_DAYS -> 2880L
          ChangeIntervals.WEEK -> 10080L
        }
      }.orElse(60L)
  }

  private fun captureTimestamp() {
    Config.instance.lastChangeTime = Instant.now().epochSecond
  }
}
