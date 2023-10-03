package io.unthrottled.theme.randomizer.themes

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.laf.UIThemeLookAndFeelInfo
import com.intellij.ide.ui.laf.UiThemeProviderListManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.services.LAFProbabilityService
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.*
import kotlin.math.abs

@Suppress("UnstableApiUsage")
class ThemeService : Disposable {
  companion object {
    val instance: ThemeService
      get() = ApplicationManager.getApplication().getService(ThemeService::class.java)
  }

  @Suppress("UnstableApiUsage")
  private fun getRandomTheme(
    selectableThemeType: SelectableThemeType
  ): Optional<out UIThemeLookAndFeelInfo>? {
    val currentLaf = LafManager.getInstance().currentUIThemeLookAndFeel
    return LAFProbabilityService.instance.pickAssetFromList(
      getPreferredThemes(selectableThemeType).filter { it.id != currentLaf.id }
    )
  }

  private fun getPreferredThemes(selectableThemeType: SelectableThemeType) =
    UiThemeProviderListManager.Companion.getInstance().getLaFs()
      .filter {
        when (selectableThemeType) {
          SelectableThemeType.ANY -> true
          SelectableThemeType.LIGHT -> it.isDark.not()
          SelectableThemeType.DARK -> it.isDark
        }
      }
      .filter { ThemeGatekeeper.instance.isLegit(it) }
      .toMutableList()

  private fun pickNextTheme(selectableThemeType: SelectableThemeType): Optional<UIThemeLookAndFeelInfo> {
    val themes = getPreferredThemes(selectableThemeType).sortedBy { it.name }
    val currentLookAndFeel = LafManager.getInstance().currentUIThemeLookAndFeel
    val themeIndex = themes.indexOfLast {
      it.id == currentLookAndFeel.id
    }

    return themes.toOptional()
      .filter { it.isNotEmpty() }
      .map {
        it[
          (
            if (themeIndex > -1) {
              themeIndex + 1
            } else {
              abs(
                Collections.binarySearch(
                  themes,
                  currentLookAndFeel
                ) { themeOne, themeTwo ->
                  themeOne.name.compareTo(themeTwo.name)
                } + 1
              )
            }
            ) % it.size
        ]
      }
  }

  override fun dispose() {}
  fun nextTheme(
    selectableThemeType: SelectableThemeType = SelectableThemeType.ANY
  ): Optional<out UIThemeLookAndFeelInfo>? {
    // only want to check for theme selection updates when the next theme is being selected
    ThemeSelectionService.instance.reHydrateSelections()
    return if (Config.instance.isRandomOrder) {
      getRandomTheme(selectableThemeType)
    } else {
      pickNextTheme(selectableThemeType)
    }
  }
}
