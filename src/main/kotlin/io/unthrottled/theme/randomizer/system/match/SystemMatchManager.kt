package io.unthrottled.theme.randomizer.system.match

import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.laf.SystemDarkThemeDetector
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.DEFAULT_OBSERVATION_COUNT
import io.unthrottled.theme.randomizer.mode.PluginMode
import io.unthrottled.theme.randomizer.mode.toPluginMode
import io.unthrottled.theme.randomizer.themes.SelectableThemeType
import io.unthrottled.theme.randomizer.themes.ThemeService
import io.unthrottled.theme.randomizer.themes.getId
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.Optional
import javax.swing.UIManager

internal enum class SystemStateObservation {
  CHANGED, SAME,
}

enum class SystemType(val selectableThemeType: SelectableThemeType) {
  DARK(SelectableThemeType.DARK), LIGHT(SelectableThemeType.LIGHT);

  companion object {
    private val nameToType = values().associateBy { it.toString() }

    fun fromStringValue(value: String?): SystemType? =
      if (value == null) null else nameToType[value]
  }
}

internal object SystemStateObserver {

  fun observeThemeChange(currentSystemType: SystemType): SystemStateObservation {
    val lastObservedThemeKey = "randomizer.system.match.last.observed.system"
    val lastObservedSystemType = SystemType.fromStringValue(
      PropertiesComponent.getInstance().getValue(
        lastObservedThemeKey
      )
    )
    if (SystemType.DARK == currentSystemType &&
      lastObservedSystemType != SystemType.DARK
    ) {
      Config.instance.darkSystemObservedCounts++
    } else if (lastObservedSystemType != SystemType.LIGHT) {
      Config.instance.lightSystemObservedCounts++
    }

    if (lastObservedSystemType != null) {
      PropertiesComponent.getInstance().setValue(
        lastObservedThemeKey,
        lastObservedSystemType.toString()
      )
    }
    val didSystemTypeChange = lastObservedSystemType == null ||
      lastObservedSystemType != currentSystemType
    return if (didSystemTypeChange) SystemStateObservation.CHANGED
    else SystemStateObservation.SAME
  }
}

object SystemMatchManager : Disposable {

  private val messageBus = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBus.subscribe(
      ConfigListener.CONFIG_TOPIC,
      ConfigListener { newPluginState, previousConfig ->
        val newPluginStateMode = newPluginState.pluginMode.toPluginMode()
        if (newPluginState.isChangeTheme &&
          newPluginStateMode == PluginMode.SYSTEM_MATCH
        ) {
          if (previousConfig.pluginMode != newPluginStateMode) {
            // restore default observation counts
            Config.instance.lightSystemObservedCounts = DEFAULT_OBSERVATION_COUNT
            Config.instance.darkSystemObservedCounts = DEFAULT_OBSERVATION_COUNT
          }
          lafDetector.value.check()
        }
      }
    )
  }

  private val lafDetector = lazy {
    SystemDarkThemeDetector.createDetector { systemIsDark: Boolean ->
      handleSystemUpdateEvent(
        if (systemIsDark) {
          SystemType.DARK
        } else {
          SystemType.LIGHT
        }
      )
    }
  }

  fun isSystemMatchAvailable() =
    lafDetector.value.detectionSupported

  private fun handleSystemUpdateEvent(currentSystemType: SystemType) {
    if (isSystemMatch().not()) return

    val systemStateObservation = SystemStateObserver.observeThemeChange(currentSystemType)

    if (systemStateObservation == SystemStateObservation.SAME) return

    val chooseNewTheme = shouldChooseNewTheme(currentSystemType)

    if (chooseNewTheme) {
      adjustObservations(currentSystemType)
      ThemeService.instance.nextTheme(currentSystemType.selectableThemeType)
    } else {
      restorePreviousTheme(currentSystemType)
    }
      .ifPresent { lookAndFeel ->
        rememberTheme(currentSystemType, lookAndFeel)
        QuickChangeLookAndFeel.switchLafAndUpdateUI(
          LafManager.getInstance(),
          lookAndFeel,
          true
        )
      }
  }

  private fun rememberTheme(currentSystemType: SystemType, lookAndFeel: UIManager.LookAndFeelInfo) {
    val propertyKey = getPreviousThemePropertyKey(currentSystemType)
    PropertiesComponent.getInstance().setValue(
      propertyKey, lookAndFeel.getId()
    )
  }

  private fun getPreviousThemePropertyKey(systemType: SystemType) =
    when (systemType) {
      SystemType.DARK -> "randomizer.system.match.dark.previous"
      SystemType.LIGHT -> "randomizer.system.match.light.previous"
    }

  private fun restorePreviousTheme(currentSystemType: SystemType): Optional<UIManager.LookAndFeelInfo> {
    val previousThemeKey = getPreviousThemePropertyKey(currentSystemType)
    return PropertiesComponent.getInstance()
      .getValue(previousThemeKey).toOptional()
      .flatMap { themeId ->
        LafManager.getInstance().installedLookAndFeels
          .firstOrNull { laf ->
            laf.getId() == themeId
          }
          .toOptional()
      }
      .map { it.toOptional() }
      // if the property key or the saved theme isn't installed
      // pick another theme...
      .orElseGet { ThemeService.instance.nextTheme(currentSystemType.selectableThemeType) }
  }

  private fun shouldChooseNewTheme(currentSystemType: SystemType): Boolean =
    when (currentSystemType) {
      SystemType.DARK -> {
        Config.instance.darkSystemObservedCounts >
          Config.instance.changeOnSystemSwitches
      }
      SystemType.LIGHT -> {
        Config.instance.lightSystemObservedCounts >
          Config.instance.changeOnSystemSwitches
      }
    }

  private fun adjustObservations(currentSystemType: SystemType) {
    when (currentSystemType) {
      SystemType.DARK -> {
        Config.instance.darkSystemObservedCounts = 0
      }
      SystemType.LIGHT -> {
        Config.instance.lightSystemObservedCounts = 0
      }
    }
  }

  fun init() {
    if (isSystemMatch()) {
      lafDetector.value.check()
    }
  }

  private fun isSystemMatch() = Config.instance.pluginMode.toPluginMode() == PluginMode.SYSTEM_MATCH

  override fun dispose() {
    messageBus.dispose()
  }
}
