package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.Collections
import java.util.Optional
import javax.swing.UIManager
import kotlin.math.abs

enum class SelectableThemeType {
  DARK, LIGHT, ANY
}
class ThemeService : Disposable {
  companion object {
    val instance: ThemeService
      get() = ServiceManager.getService(ThemeService::class.java)
  }

  private fun getRandomTheme(): Optional<UIManager.LookAndFeelInfo> {
    val currentLaf = LafManager.getInstance().currentLookAndFeel
    return LAFProbabilityService.instance.pickAssetFromList(
      getPreferredThemes().filter { it.getId() != currentLaf.getId() }
    )
  }

  private fun getPreferredThemes() =
    LafManager.getInstance().installedLookAndFeels
      .filter { ThemeGatekeeper.instance.isLegit(it) }

  private fun pickNextTheme(): Optional<UIManager.LookAndFeelInfo> {
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
                currentLookAndFeel
              ) { themeOne, themeTwo ->
                themeOne.name.compareTo(themeTwo.name)
              } + 1
            )
            ) % it.size
        ]
      }
  }

  override fun dispose() {}
  fun nextTheme(selectableThemeType: SelectableThemeType = SelectableThemeType.ANY): Optional<UIManager.LookAndFeelInfo> =
    if (Config.instance.isRandomOrder) {
      getRandomTheme()
    } else pickNextTheme()
}
