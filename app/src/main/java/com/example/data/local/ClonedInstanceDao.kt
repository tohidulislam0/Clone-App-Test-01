package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClonedInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClonedInstanceDao {
  @Query("SELECT * FROM cloned_instances ORDER BY isPinnedFavorite DESC, lastActiveAt DESC")
  fun observeAllInstances(): Flow<List<ClonedInstanceEntity>>

  @Query("SELECT * FROM cloned_instances WHERE id = :id LIMIT 1")
  suspend fun getInstanceById(id: Long): ClonedInstanceEntity?

  @Query("SELECT COUNT(*) FROM cloned_instances WHERE platformId = :platformId")
  suspend fun getInstanceCountForPlatform(platformId: String): Int

  @Query("SELECT COUNT(*) FROM cloned_instances")
  suspend fun getTotalInstanceCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInstance(instance: ClonedInstanceEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(instances: List<ClonedInstanceEntity>)

  @Update
  suspend fun updateInstance(instance: ClonedInstanceEntity)

  @Query("DELETE FROM cloned_instances WHERE id = :id")
  suspend fun deleteInstanceById(id: Long)

  @Query(
    """
    UPDATE cloned_instances 
    SET lastVisitedUrl = :url,
        cookieJarJson = :cookieJarJson,
        localStorageJson = :localStorageJson,
        sessionStorageJson = :sessionStorageJson,
        cookieCount = :cookieCount,
        storageKeyCount = :storageKeyCount,
        blockedTrackersCount = :blockedTrackersCount,
        lastActiveAt = :timestamp
    WHERE id = :id
    """
  )
  suspend fun updateSessionSnapshot(
    id: Long,
    url: String,
    cookieJarJson: String,
    localStorageJson: String,
    sessionStorageJson: String,
    cookieCount: Int,
    storageKeyCount: Int,
    blockedTrackersCount: Int,
    timestamp: Long = System.currentTimeMillis(),
  )

  @Query(
    """
    UPDATE cloned_instances
    SET cookieJarJson = '{}',
        localStorageJson = '{}',
        sessionStorageJson = '{}',
        cookieCount = 0,
        storageKeyCount = 0,
        lastVisitedUrl = baseUrl,
        lastActiveAt = :timestamp
    WHERE id = :id
    """
  )
  suspend fun clearInstanceSession(id: Long, timestamp: Long = System.currentTimeMillis())

  @Query(
    """
    UPDATE cloned_instances
    SET launchCount = launchCount + 1,
        lastActiveAt = :timestamp
    WHERE id = :id
    """
  )
  suspend fun incrementLaunchCount(id: Long, timestamp: Long = System.currentTimeMillis())
}
