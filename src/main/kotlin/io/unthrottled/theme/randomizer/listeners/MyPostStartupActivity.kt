package io.unthrottled.theme.randomizer.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.unthrottled.theme.randomizer.PluginMaster

/**
 * A class representing a post-startup activity for the plugin.
 * This class is responsible for executing the necessary actions after the plugin is started.
 */
internal class MyPostStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) = PluginMaster.instance.projectOpened()
}
