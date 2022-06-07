package io.unthrottled.theme.randomizer.services

import io.unthrottled.theme.randomizer.themes.LocalPersistenceService
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.max

enum class AssetCategory(val directory: String) {
  META("meta")
}

data class ThemeObservationLedger(
  val assetSeenCounts: ConcurrentMap<String, Int>,
  val writeDate: Instant,
)

object ThemeObservationService : LocalPersistenceService<ThemeObservationLedger> (
  "seen-themes-ledger.json",
  ThemeObservationLedger::class.java
) {

  private const val MAX_ALLOWED_DAYS_PERSISTED = 7L

  override fun decorateItem(item: ThemeObservationLedger): ThemeObservationLedger =
    if (Duration.between(item.writeDate, Instant.now()).toDays() >= MAX_ALLOWED_DAYS_PERSISTED) {
      // reset counts so that way new assets that haven't been seen yet will eventually make
      // it to the ledger. Also enables users to see their favorite (most seen) assets again
      item.copy(
        assetSeenCounts = item.assetSeenCounts.entries.stream()
          .map { it.key to 1 }
          .collect(
            Collectors.toConcurrentMap({ it.first }, { it.second }) { _, theChosenOne -> theChosenOne }
          )
      )
    } else {
      item
    }
  override fun buildDefaultLedger() = ThemeObservationLedger(ConcurrentHashMap(), Instant.now())

  override fun combineWithOnDisk(themeObservationLedger: ThemeObservationLedger): ThemeObservationLedger {
    val onDisk = readLedger()
    return themeObservationLedger.copy(
      assetSeenCounts = Stream.concat(
        onDisk.assetSeenCounts.entries.stream(),
        themeObservationLedger.assetSeenCounts.entries.stream()
      ).collect(
        Collectors.toConcurrentMap(
          { it.key },
          { it.value },
          { ogAssetCount, thisIDEsCount ->
            // The user has many IDEs, so if one IDE is used more than the
            // other, (IDEs share the same asset source)
            // we want to remember that they've seen that asset a fair amount of times.
            max(ogAssetCount, thisIDEsCount)
          }
        ) { ConcurrentHashMap() }
      )
    )
  }
}
