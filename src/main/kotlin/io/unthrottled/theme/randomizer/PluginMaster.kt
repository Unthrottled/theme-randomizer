package io.unthrottled.theme.randomizer

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import io.unthrottled.theme.randomizer.onboarding.UserOnBoarding
import io.unthrottled.theme.randomizer.services.LAFProbabilityService
import io.unthrottled.theme.randomizer.system.match.SystemMatchManager
import io.unthrottled.theme.randomizer.timed.ThemeChangeEventEmitter
import io.unthrottled.theme.randomizer.tools.Logging

class PluginMaster : Disposable, Logging {

  companion object {
    val instance: PluginMaster
      get() = ApplicationManager.getApplication().getService(PluginMaster::class.java)
  }

  init {
    LAFProbabilityService.instance.toString() // wake up the look and feel probability service
    SystemMatchManager.init()
  }

  private val themeChangeEventEmitter = ThemeChangeEventEmitter()

  fun projectOpened(project: Project) {
    registerListenersForProject(project)
  }

  private fun registerListenersForProject(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
  }

  override fun dispose() {
    themeChangeEventEmitter.dispose()
    SystemMatchManager.dispose()
  }

  fun onUpdate() {
    ProjectManager.getInstance().openProjects
      .forEach { registerListenersForProject(it) }
  }
}
