package io.unthrottled.theme.randomizer.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import com.intellij.util.xmlb.XmlSerializerUtil.createCopy
import io.unthrottled.theme.randomizer.config.actors.LafAnimationActor

data class ConfigSettingsModel(
  var interval: String,
  var isChangeTheme: Boolean,
  var isRandomOrder: Boolean,
  var isThemeTransition: Boolean,
) {
  fun duplicate(): ConfigSettingsModel = copy()
}

@State(
  name = "Theme-Randomizer-Config",
  storages = [Storage("theme-randomizer.xml")]
)
class Config : PersistentStateComponent<Config>, Cloneable {
  companion object {
    @JvmStatic
    val instance: Config
      get() = ServiceManager.getService(Config::class.java)
    const val DEFAULT_DELIMITER = ","

    @JvmStatic
    fun getInitialConfigSettingsModel() = ConfigSettingsModel(
      interval = instance.interval,
      isChangeTheme = instance.isChangeTheme,
      isRandomOrder = instance.isRandomOrder,
      isThemeTransition = LafAnimationActor.getAnimationEnabled(),
    )
  }

  var interval: String = ChangeIntervals.DAY.toString()
  var isChangeTheme: Boolean = true
  var isRandomOrder: Boolean = true
  var userId: String = ""
  var version: String = ""
  var selectedThemes = ""
  var lastChangeTime = -1L

  override fun getState(): Config? =
    createCopy(this)

  override fun loadState(state: Config) {
    copyBean(state, this)
  }
}
