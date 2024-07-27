@file:Suppress("UnstableApiUsage")

package io.unthrottled.theme.randomizer.timed

import com.intellij.ide.IdeEventQueue
import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.ide.ui.laf.UIThemeLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.Alarm
import io.unthrottled.theme.randomizer.config.ChangeIntervals
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.mode.PluginMode
import io.unthrottled.theme.randomizer.mode.getCurrentSelectableThemeType
import io.unthrottled.theme.randomizer.mode.toPluginMode
import io.unthrottled.theme.randomizer.themes.ThemeService
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@SuppressWarnings("TooManyFunctions")
class ThemeChangeEventEmitter : Runnable, LafManagerListener, Disposable {
  private val messageBus = ApplicationManager.getApplication().messageBus.connect()
  private val themeChangeAlarm = Alarm()

  private val reSubscriber = {
    if (themeChangeAlarm.isEmpty && Config.instance.isChangeTheme) {
      val duration = getDuration()
      themeChangeAlarm.addRequest(this, convertMinutesToMillis(duration))
    }
  }

  init {
    val self = this
    messageBus.subscribe(
      ConfigListener.CONFIG_TOPIC,
      ConfigListener { newPluginState, _ ->
        themeChangeAlarm.cancelAllRequests()
        if (newPluginState.isChangeTheme && newPluginState.pluginMode.toPluginMode() == PluginMode.TIMED) {
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
      this
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
    if (Config.instance.lastChangeTime < 0) captureTimestamp()
    return getThemeSwitchCheckInterval()
  }

  private fun scheduleThemeChange() {
    themeChangeAlarm.addRequest(this, convertMinutesToMillis(getThemeSwitchCheckInterval()))
  }

  private fun convertMinutesToMillis(duration: Long) = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.MINUTES).toInt()

  private fun getThemeSwitchCheckInterval(): Long = getThemeSwitchCheckIntervalFromState(Config.instance)

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

  private var themeSet: UIThemeLookAndFeelInfo = LafManager.getInstance().currentUIThemeLookAndFeel

  override fun run() {
    if (Config.instance.isChangeTheme.not() || isTime(Config.instance).not()) return

    ThemeService.instance.nextTheme(getCurrentSelectableThemeType())
      ?.ifPresent {
        QuickChangeLookAndFeel.switchLafAndUpdateUI(LafManager.getInstance(), it, true)
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
          ChangeIntervals.MINUTE -> 1L
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
    val currentLookAndFeel = source.currentUIThemeLookAndFeel
    if (currentLookAndFeel.id != themeSet.id) {
      themeSet = currentLookAndFeel
      captureTimestamp()
    }
  }

  companion object {
    private const val MAX_CHECK_INTERVAL = 5L
  }
}
