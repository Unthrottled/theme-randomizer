package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigListener.Companion.CONFIG_TOPIC
import java.util.Optional
import javax.swing.UIManager

class ThemeService : Disposable {
  companion object {
    val instance: ThemeGatekeeper
      get() = ServiceManager.getService(ThemeGatekeeper::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
      }
    )
  }

  fun getRandomTheme(): Optional<UIManager.LookAndFeelInfo> =
    LAFProbabilityService.instance.pickAssetFromList(
      LafManager.getInstance().installedLookAndFeels
        .filter { ThemeGatekeeper.instance.isLegit(it) }
    )

  override fun dispose() {
    connection.dispose()
  }
}
