package io.unthrottled.theme.randomizer.config

import io.unthrottled.theme.randomizer.tools.runSafelyWithResult
import io.unthrottled.theme.randomizer.tools.toOptional
import java.util.Optional

enum class ChangeIntervals {
  FIVE_MINUTES,
  FIFTEEN_MINUTES,
  THIRTY_MINUTES,
  HOUR,
  DAY;

  companion object {
    fun getValue(value: String): Optional<ChangeIntervals> =
      runSafelyWithResult({
        valueOf(value).toOptional()
      }) { Optional.empty() }
  }
}
