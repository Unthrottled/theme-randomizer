package io.unthrottled.theme.randomizer.services

import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.theme.randomizer.tools.runSafelyWithResult
import io.unthrottled.theme.randomizer.tools.toOptional
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.time.Instant
import java.util.Optional
import kotlin.io.path.exists

data class Lock(
  val lockedBy: String,
  val lockedDate: Instant
)

object LockMaster {
  private val log = Logger.getInstance(LockMaster::class.java)

  private val gson = GsonBuilder()
    .setPrettyPrinting()
    .create()

  private val lockPath =
    LocalStorageService.constructLocalContentPath(
      AssetCategory.META,
      "lock.json"
    )

  fun acquireLock(id: String): Boolean =
    when {
      Files.notExists(lockPath) -> lockProcess(id)
      canBreakLock() -> breakAndLockProcess(id)
      else -> false
    }

  private fun canBreakLock(): Boolean =
    readLock()
      .map {
        Duration.between(
          it.lockedDate,
          Instant.now()
        ).toHours() > 1
      }
      .orElse(true)

  private fun breakAndLockProcess(id: String): Boolean =
    if (breakLock()) {
      lockProcess(id)
    } else {
      false
    }

  fun releaseLock(id: String) {
    if (holdingLock(id)) {
      breakLock()
    }
  }

  private fun holdingLock(id: String): Boolean =
    readLock()
      .map { it.lockedBy == id }
      .orElse(false)

  private fun breakLock(): Boolean =
    if (Files.exists(lockPath)) {
      runSafelyWithResult({
        Files.delete(lockPath)
        true
      }) {
        log.warn("Unable to remove previous lock for raisins", it)
        false
      }
    } else {
      true
    }

  private fun lockProcess(id: String): Boolean =
    writeLock(Lock(id, Instant.now()))

  private fun readLock(): Optional<Lock> =
    runSafelyWithResult({
      Files.newInputStream(lockPath)
        .use {
          gson.fromJson(
            InputStreamReader(it, StandardCharsets.UTF_8),
            Lock::class.java
          )
        }.toOptional()
    }) {
      log.warn("Unable to read promotion ledger for raisins.", it)
      Optional.empty()
    }

  private fun writeLock(lock: Lock): Boolean =
    runSafelyWithResult({
      if (lockPath.parent.exists().not()) {
        LocalStorageService.createDirectories(lockPath)
      }
      Files.newBufferedWriter(lockPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        .use {
          it.write(gson.toJson(lock))
        }
      true
    }) {
      log.warn("Unable to write theme lock for raisins.", it)
      false
    }
}
