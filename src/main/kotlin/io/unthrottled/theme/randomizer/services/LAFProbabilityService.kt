package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.ide.ui.laf.UIThemeLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import io.unthrottled.theme.randomizer.tools.AlarmDebouncer
import io.unthrottled.theme.randomizer.tools.ProbabilityTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.random.Random

@Suppress("UnstableApiUsage")
@Service(Service.Level.APP)
class LAFProbabilityService : Disposable, LafManagerListener, Runnable {
  private val debouncer = AlarmDebouncer<UIThemeLookAndFeelInfo>(
    TimeUnit.MILLISECONDS.convert(
      THEME_DEBOUNCE_DURATION_IN_MINUTES,
      TimeUnit.MINUTES
    ).toInt()
  )

  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

  private val scope = CoroutineScope(Dispatchers.Default)
  private val idleJob = scope.launch {
    delay(
      TimeUnit.MILLISECONDS.convert(
        DEFAULT_IDLE_TIMEOUT_IN_MINUTES,
        TimeUnit.MINUTES
      )
    )
    run()
  }

  init {
    messageBusConnection.subscribe(LafManagerListener.TOPIC, this)
    idleJob.start()
  }

  private val seenAssetLedger = ThemeObservationService.getInitialItem()

  private val random = java.util.Random()

  private val probabilityTools = ProbabilityTools(Random(System.currentTimeMillis()))

  fun pickAssetFromList(themes: Collection<UIThemeLookAndFeelInfo>): Optional<out UIThemeLookAndFeelInfo>? {
    val seenTimes = themes.map { getSeenCount(it) }
    val maxSeen = seenTimes.stream().mapToInt { it }.max().orElse(0)
    val totalItems = themes.size
    return probabilityTools.pickFromWeightedList(
      themes.map {
        val timesObserved = getSeenCount(it)
        it to 1 + (abs(random.nextGaussian()) * totalItems.toDouble().pow(maxSeen - timesObserved)).toLong()
      }.shuffled(random)
    )
  }

  // give strong bias to assets that haven't
  // been seen by the user during the ledger cycle duration see:
  // <code>MAX_ALLOWED_DAYS_PERSISTED</code> in AssetObservationService for
  // more details
  private fun getSeenCount(it: UIThemeLookAndFeelInfo) =
    seenAssetLedger.assetSeenCounts.getOrDefault(it.id, 0)

  override fun dispose() {
    messageBusConnection.dispose()
    ThemeObservationService.persistLedger(seenAssetLedger)
    idleJob.cancel()
  }

  @Suppress("UnstableApiUsage")
  private fun onChanged(lookAndFeelInfo: UIThemeLookAndFeelInfo) {
    val themeId = lookAndFeelInfo.id
    seenAssetLedger.assetSeenCounts[themeId] = getAssetSeenCount(themeId) + 1
  }

  // This prevents newly seen assets from always being
  // biased to be shown to the user.
  private fun getAssetSeenCount(lookAndFeelId: String): Int {
    val seenAssets = seenAssetLedger.assetSeenCounts
    return when {
      seenAssets.containsKey(lookAndFeelId) -> seenAssets[lookAndFeelId]!!
      else                                  -> floor(seenAssets.entries.map { it.value }.average()).toInt()
    }
  }

  override fun run() {
    val updatedLedger = ThemeObservationService.persistLedger(seenAssetLedger)
    updatedLedger.assetSeenCounts.forEach { (assetId, assetSeenCount) ->
      seenAssetLedger.assetSeenCounts[assetId] = assetSeenCount
    }
  }

  @Suppress("UnstableApiUsage")
  override fun lookAndFeelChanged(source: LafManager) {
    val currentTheme = source.currentUIThemeLookAndFeel
    debouncer.debounce { onChanged(currentTheme) }
  }

  companion object {
    val instance: LAFProbabilityService
      get() = ApplicationManager.getApplication().getService(LAFProbabilityService::class.java)

    internal const val DEFAULT_IDLE_TIMEOUT_IN_MINUTES = 5L
    internal const val THEME_DEBOUNCE_DURATION_IN_MINUTES = 2L
  }

}
