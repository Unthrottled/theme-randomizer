package io.unthrottled.theme.randomizer.onboarding

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.Constants.PLUGIN_ID
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.*

object UserOnBoarding {

  fun attemptToPerformNewUpdateActions() {
    getNewVersion().ifPresent { newVersion ->
      Config.instance.version = newVersion
    }
    if (Config.instance.userId.isEmpty()) {
      Config.instance.userId = UUID.randomUUID().toString()
    }
  }

  private fun getNewVersion() =
    getVersion()
      .filter { it != Config.instance.version }

  private fun getVersion(): Optional<String> =
    PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
      .toOptional()
      .map { it.version }
}
