package io.unthrottled.theme.randomizer.themes

import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigSettingsModel
import io.unthrottled.theme.randomizer.services.AppService.getApplicationName
import io.unthrottled.theme.randomizer.services.LockMaster
import io.unthrottled.theme.randomizer.services.LockMaster.releaseLock
import io.unthrottled.theme.randomizer.tools.runSafely

class ThemeSelectionConfigListener : ConfigListener {
  override fun pluginConfigUpdated(
    config: Config,
    previousConfig: ConfigSettingsModel
  ) {
    ThemeSelectionService.instance.rehydrateSelectionsFromConfig(config)

    if (config.isLocalSync) {
      // sync config changes
      runSafely({
        if (LockMaster.acquireLock(lockId)) {
          LocalThemeSelectionService.saveSelections(config)
          releaseLock(lockId)
        }
      }) {
        releaseLock(lockId)
      }
    }
  }

  private val lockId: String
    get() = getApplicationName()
}
