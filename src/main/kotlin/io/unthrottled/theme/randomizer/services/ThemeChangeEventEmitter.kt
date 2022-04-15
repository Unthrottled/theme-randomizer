package io.unthrottled.theme.randomizer.services

import com.intellij.ide.IdeEventQueue
import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.Alarm
import io.unthrottled.theme.randomizer.config.ChangeIntervals
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.theme.randomizer.mode.PluginMode
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@SuppressWarnings("TooManyFunctions")
class ThemeChangeEventEmitter : Runnable, LafManagerListener, Disposable {
  private val messageBus = ApplicationManager.getApplication().messageBus.connect()
  private val themeChangeAlarm = Alarm()

  companion object {
    private const val MAX_CHECK_INTERVAL = 5L
  }

  private val reSubscriber = {
    if (themeChangeAlarm.isEmpty && Config.instance.isChangeTheme) {
      val duration = getDuration()
      themeChangeAlarm.addRequest(
        this,
        convertMinutesToMillis(duration)
      )
    }
  }

  init {
    val self = this
    messageBus.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        themeChangeAlarm.cancelAllRequests()
        if (newPluginState.isChangeTheme &&
          PluginMode.valueFrom(newPluginState.pluginMode) == PluginMode.TIMED) {
          themeChangeAlarm.addRequest(
            self,
            convertMinutesToMillis(
              getThemeSwitchCheckIntervalFromState(newPluginState)
            )
          )
        }
      }
    )
    messageBus.subscribe(
      LafManagerListener.TOPIC,
      this,
    )
    themeChangeAlarm.addRequest(
      this,
      convertMinutesToMillis(getDuration())
    )
    IdeEventQueue.getInstance().addIdleListener(
      reSubscriber,
      convertMinutesToMillis(MAX_CHECK_INTERVAL)
    )
  }

  private fun getDuration(): Long {
    if (Config.instance.lastChangeTime < 0) {
      captureTimestamp()
    }
    return getThemeSwitchCheckInterval()
  }

  private fun scheduleThemeChange() {
    themeChangeAlarm.addRequest(
      this,
      convertMinutesToMillis(getThemeSwitchCheckInterval())
    )
  }

  private fun convertMinutesToMillis(duration: Long) = TimeUnit.MILLISECONDS.convert(
    duration,
    TimeUnit.MINUTES
  ).toInt()

  private fun getThemeSwitchCheckInterval(): Long =
    getThemeSwitchCheckIntervalFromState(Config.instance)

  private fun getThemeSwitchCheckIntervalFromState(newPluginState: Config): Long =
    minOf(
      maxOf(
        0L,
        getThemeChangeIntervalInMinutes(newPluginState) - getDurationSinceThemeChange().toMinutes()
      ),
      MAX_CHECK_INTERVAL
    )

  override fun dispose() {
    messageBus.dispose()
    themeChangeAlarm.dispose()
    IdeEventQueue.getInstance().removeIdleListener(reSubscriber)
  }

  private var themeSet = LafManager.getInstance().currentLookAndFeel

  override fun run() {
    if (Config.instance.isChangeTheme.not() || isTime(Config.instance).not()) return

    ThemeService.instance.nextTheme()
      .ifPresent {
        QuickChangeLookAndFeel.switchLafAndUpdateUI(
          LafManager.getInstance(),
          it,
          true
        )
        themeSet = it
        captureTimestamp()
      }
    scheduleThemeChange()
  }

  private fun isTime(config: Config): Boolean =
    getThemeChangeIntervalInMinutes(config) <= getDurationSinceThemeChange().toMinutes()

  private fun getDurationSinceThemeChange() = Duration.between(
    Instant.ofEpochSecond(Config.instance.lastChangeTime),
    Instant.now()
  )

  @SuppressWarnings("MagicNumber")
  private fun getThemeChangeIntervalInMinutes(config: Config): Long =
    ChangeIntervals.getValue(config.interval)
      .map {
        when (it) {
          ChangeIntervals.THIRTY_MINUTES -> 30L
          ChangeIntervals.HOUR -> 60L
          ChangeIntervals.DAY -> 1440L
          ChangeIntervals.TWO_DAYS -> 2880L
          ChangeIntervals.WEEK -> 10080L
          else -> 60L
        }
      }.orElse(60L)

  private fun captureTimestamp() {
    Config.instance.lastChangeTime = Instant.now().epochSecond
  }

  override fun lookAndFeelChanged(source: LafManager) {
    val currentLookAndFeel = source.currentLookAndFeel
    if (currentLookAndFeel.getId() != themeSet.getId()) {
      themeSet = currentLookAndFeel
      captureTimestamp()
    }
  }
}
