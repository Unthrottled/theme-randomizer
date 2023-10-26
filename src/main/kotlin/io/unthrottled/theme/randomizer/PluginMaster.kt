package io.unthrottled.theme.randomizer

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.ProjectManager
import io.unthrottled.theme.randomizer.onboarding.UserOnBoarding
import io.unthrottled.theme.randomizer.services.LAFProbabilityService
import io.unthrottled.theme.randomizer.system.match.SystemMatchManager
import io.unthrottled.theme.randomizer.timed.ThemeChangeEventEmitter
import io.unthrottled.theme.randomizer.tools.Logging

@Service(Service.Level.APP)
class PluginMaster : Disposable, Logging {

  init {
    LAFProbabilityService.instance.toString() // wake up the look and feel probability service
    SystemMatchManager.init()
  }

  private val themeChangeEventEmitter = ThemeChangeEventEmitter()

  fun projectOpened() = registerListenersForProject()

  private fun registerListenersForProject() = UserOnBoarding.attemptToPerformNewUpdateActions()

  override fun dispose() {
    themeChangeEventEmitter.dispose()
    SystemMatchManager.dispose()
  }

  fun onUpdate() = repeat(ProjectManager.getInstance().openProjects.count()) { registerListenersForProject() }

  companion object {
    val instance: PluginMaster
      get() = ApplicationManager.getApplication().getService(PluginMaster::class.java)
  }

}
