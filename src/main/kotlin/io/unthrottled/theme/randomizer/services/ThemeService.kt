package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
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

  fun getRandomTheme(): Optional<UIManager.LookAndFeelInfo> =
    LAFProbabilityService.instance.pickAssetFromList(
      getPreferredThemes().shuffled()
    )

  private fun getPreferredThemes() =
    LafManager.getInstance().installedLookAndFeels
      .filter { ThemeGatekeeper.instance.isLegit(it) }

  fun getNextTheme(): Optional<UIManager.LookAndFeelInfo> {
    val themes = getPreferredThemes().sortedBy { it.name }
    val currentLookAndFeel = LafManager.getInstance().currentLookAndFeel
    val themeIndex = themes.indexOfLast {
      it.getId() == currentLookAndFeel.getId()
    }

    return themes.toOptional()
      .filter { it.isNotEmpty() }
      .map {
        it[
          (
            if (themeIndex > -1) themeIndex + 1 else abs(
              Collections.binarySearch(
                themes,
                currentLookAndFeel,
                { themeOne, themeTwo ->
                  themeOne.name.compareTo(themeTwo.name)
                }
              ) + 1
            )
            ) % it.size
        ]
      }
  }

  override fun dispose() {}
}
