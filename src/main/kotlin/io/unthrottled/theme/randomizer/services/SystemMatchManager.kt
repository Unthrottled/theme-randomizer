package io.unthrottled.theme.randomizer.services

import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.laf.SystemDarkThemeDetector
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.mode.PluginMode
import io.unthrottled.theme.randomizer.mode.toPluginMode
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.Optional
import javax.swing.UIManager

enum class SystemType(val selectableThemeType: SelectableThemeType) {
  DARK(SelectableThemeType.DARK), LIGHT(SelectableThemeType.LIGHT);

  companion object {
    private val nameToType = values().associateBy { it.toString() }

    fun fromStringValue(value: String?): SystemType? =
      if (value == null) null else nameToType[value]
  }
}

object SystemMatchManager : Disposable {

  private val messageBus = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBus.subscribe(
      ConfigListener.CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        if (newPluginState.isChangeTheme &&
          newPluginState.pluginMode.toPluginMode() == PluginMode.SYSTEM_MATCH
        ) {
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

    observeThemeChange(currentSystemType)

    val chooseNewTheme = shouldChooseNewTheme(currentSystemType)

    // todo: shouldn't really switch theme if current laf & system match
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

  private fun observeThemeChange(currentSystemType: SystemType) {
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
