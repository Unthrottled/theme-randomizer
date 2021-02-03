package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.Collections
import java.util.Optional
import javax.swing.UIManager
import kotlin.math.abs

class ThemeService : Disposable {
  companion object {
    val instance: ThemeService
      get() = ServiceManager.getService(ThemeService::class.java)
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
      getPreferredThemes()
    )

  private fun getPreferredThemes() = LafManager.getInstance().installedLookAndFeels
    .filter { ThemeGatekeeper.instance.isLegit(it) }

  fun getNextTheme(): Optional<UIManager.LookAndFeelInfo> {
    val themes = getPreferredThemes().sortedBy { it.getId() }
    val themeIndex = Collections.binarySearch(
      themes,
      LafManager.getInstance().currentLookAndFeel,
      { themeOne, themeTwo ->
        themeOne.getId().compareTo(themeTwo.getId())
      }
    )

    return themes.toOptional()
      .filter { it.isNotEmpty() }
      .map { it[(if (themeIndex > -1) themeIndex + 1 else abs(themeIndex)) % it.size] }
  }

  override fun dispose() {
    connection.dispose()
  }
}
