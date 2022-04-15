package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.laf.SystemDarkThemeDetector
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.mode.PluginMode
import io.unthrottled.theme.randomizer.mode.toPluginMode

object SystemMatchManager: Disposable {

  private val messageBus = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBus.subscribe(
      ConfigListener.CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        if (newPluginState.isChangeTheme &&
          newPluginState.pluginMode.toPluginMode() == PluginMode.SYSTEM_MATCH) {
          lafDetector.value.check()
        }
      }
    )
  }

  private val lafDetector = lazy {
    SystemDarkThemeDetector.createDetector { systemIsDark: Boolean ->
      handleSystemUpdateEvent(systemIsDark)
    }
  }

  // todo: don't show system match in settings unless available.
  fun isSystemMatchAvailable() = lafDetector.value.detectionSupported

  private fun handleSystemUpdateEvent(systemIsDark: Boolean) {
    if(isSystemMatch().not()) return

    // todo: system match stuffo
  }

  fun init() {
    if(isSystemMatch()) {
      lafDetector.value.check()
    }
  }

  private fun isSystemMatch() = Config.instance.pluginMode.toPluginMode() == PluginMode.SYSTEM_MATCH

  override fun dispose() {
    messageBus.dispose()
  }
}
