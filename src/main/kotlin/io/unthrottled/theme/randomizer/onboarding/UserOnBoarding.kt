package io.unthrottled.theme.randomizer.onboarding

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import io.unthrottled.theme.randomizer.UpdateNotification
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.Constants.PLUGIN_ID
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.Optional
import java.util.UUID

object UserOnBoarding {

  fun attemptToPerformNewUpdateActions(project: Project) {
    getNewVersion().ifPresent { newVersion ->
      Config.instance.version = newVersion
      StartupManager.getInstance(project)
        .runWhenProjectIsInitialized {
          UpdateNotification.display(project, newVersion)
        }
    }
    if (Config.instance.userId.isEmpty()) {
      Config.instance.userId = UUID.randomUUID().toString()
    }
  }

  private fun getNewVersion() =
    getVersion()
      .filter { it != Config.instance.version }

  fun getVersion(): Optional<String> =
    PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
      .toOptional()
      .map { it.version }
}
