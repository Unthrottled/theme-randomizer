package io.unthrottled.theme.randomizer.services

import com.intellij.openapi.application.ApplicationNamesInfo

object AppService {
  fun getApplicationName(): String =
    ApplicationNamesInfo.getInstance().fullProductNameWithEdition
}
