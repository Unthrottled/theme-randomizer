package io.unthrottled.theme.randomizer.config

import io.unthrottled.theme.randomizer.tools.runSafelyWithResult
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.*

enum class ChangeIntervals {
  MINUTE,
  FIVE_MINUTES,
  TEN_MINUTES,
  FIFTEEN_MINUTES,
  THIRTY_MINUTES,
  HOUR,
  DAY,
  TWO_DAYS,
  WEEK
  ;

  companion object {
    fun getValue(value: String): Optional<ChangeIntervals> =
      runSafelyWithResult({ valueOf(value).toOptional() }) { Optional.empty() }
  }
}
