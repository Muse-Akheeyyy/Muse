package io.github.museakheeyyy.muse.shared.db.daos

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import io.github.museakheeyyy.muse.shared.scrobble.PendingScrobble
import io.github.museakheeyyy.muse.shared.scrobble.ScrobbleProvider

@Dao
interface PendingScrobbleDao {
    @Query("SELECT * FROM pending_scrobble ORDER BY rowid")
    suspend fun all(): List<PendingScrobble>

    @Query("SELECT * FROM pending_scrobble WHERE id = :id")
    suspend fun get(id: String): PendingScrobble?

    @Upsert
    suspend fun upsert(scrobbles: List<PendingScrobble>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun importLegacy(scrobbles: List<PendingScrobble>)

    @Query("DELETE FROM pending_scrobble WHERE id = :id")
    suspend fun remove(id: String)

    @Query("DELETE FROM pending_scrobble WHERE provider = :provider")
    suspend fun clear(provider: ScrobbleProvider)
}
