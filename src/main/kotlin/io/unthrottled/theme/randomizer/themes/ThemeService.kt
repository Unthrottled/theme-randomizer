package io.unthrottled.theme.randomizer.themes

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ui.isDark
import io.unthrottled.theme.randomizer.services.LAFProbabilityService
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.Collections
import java.util.Optional
import javax.swing.UIManager
import kotlin.math.abs

class ThemeService : Disposable {
  companion object {
    val instance: ThemeService
      get() = ApplicationManager.getApplication().getService(ThemeService::class.java)
  }

  private fun getRandomTheme(
    selectableThemeType: SelectableThemeType
  ): Optional<UIManager.LookAndFeelInfo> {
    val currentLaf = LafManager.getInstance().currentLookAndFeel
    return LAFProbabilityService.instance.pickAssetFromList(
      getPreferredThemes(selectableThemeType).filter { it.getId() != currentLaf.getId() }
    )
  }

  private fun getPreferredThemes(selectableThemeType: SelectableThemeType) =
    LafManager.getInstance().installedLookAndFeels
      .filter {
        when (selectableThemeType) {
          SelectableThemeType.ANY -> true
          SelectableThemeType.LIGHT -> it.isDark().not()
          SelectableThemeType.DARK -> it.isDark()
        }
      }
      .filter { ThemeGatekeeper.instance.isLegit(it) }

  private fun pickNextTheme(selectableThemeType: SelectableThemeType): Optional<UIManager.LookAndFeelInfo> {
    val themes = getPreferredThemes(selectableThemeType).sortedBy { it.name }
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
  fun nextTheme(
    selectableThemeType: SelectableThemeType = SelectableThemeType.ANY
  ): Optional<UIManager.LookAndFeelInfo> {
    // only want to check for theme selection updates when the next theme is being selected
    ThemeSelectionService.instance.reHydrateIfNecessary()
    return if (Config.instance.isRandomOrder) {
      getRandomTheme(selectableThemeType)
    } else pickNextTheme(selectableThemeType)
  }
}
