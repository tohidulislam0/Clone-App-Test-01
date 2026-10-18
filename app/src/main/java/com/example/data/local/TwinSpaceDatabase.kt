package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.UserAgentMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [ClonedInstanceEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class TwinSpaceDatabase : RoomDatabase() {
  abstract fun clonedInstanceDao(): ClonedInstanceDao

  companion object {
    @Volatile
    private var INSTANCE: TwinSpaceDatabase? = null

    fun getInstance(context: Context): TwinSpaceDatabase {
      return INSTANCE ?: synchronized(this) {
        val db = Room.databaseBuilder(
          context.applicationContext,
          TwinSpaceDatabase::class.java,
          "twinspace_sandbox.db",
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
        INSTANCE = db
        CoroutineScope(Dispatchers.IO).launch {
          seedDefaultClonesIfEmpty(db.clonedInstanceDao())
        }
        db
      }
    }

    suspend fun seedDefaultClonesIfEmpty(dao: ClonedInstanceDao) {
      if (dao.getTotalInstanceCount() > 0) return
      val now = System.currentTimeMillis()
      val starterClones = listOf(
        ClonedInstanceEntity(
          platformId = "instagram",
          platformName = "Instagram",
          monogram = "IG",
          instanceName = "Personal Feed",
          badgeLabel = "#1",
          accountHandle = "@alex.personal",
          workspaceGroup = "Personal",
          baseUrl = "https://www.instagram.com/",
          lastVisitedUrl = "https://www.instagram.com/",
          accentColorHex = 0xFF00F0FFL,
          userAgentMode = UserAgentMode.ANDROID_MOBILE.name,
          privacyTrackerBlock = true,
          spoofDeviceFingerprint = true,
          quickNotes = "Primary personal Instagram account • Friends & Close Circle",
          cookieJarJson = """{"ig_cb":"1","ig_did":"A19F-882C-001","csrftoken":"ts_inst1_99a"}""",
          localStorageJson = """{"ig_theme":"dark","sandbox_slot":"instance_1"}""",
          cookieCount = 3,
          storageKeyCount = 2,
          blockedTrackersCount = 14,
          launchCount = 12,
          isPinnedFavorite = true,
          createdAt = now - 86_400_000L * 5,
          lastActiveAt = now - 120_000L,
        ),
        ClonedInstanceEntity(
          platformId = "instagram",
          platformName = "Instagram",
          monogram = "IG",
          instanceName = "Creator Studio",
          badgeLabel = "#2",
          accountHandle = "@neonprism.studio",
          workspaceGroup = "Creator",
          baseUrl = "https://www.instagram.com/",
          lastVisitedUrl = "https://www.instagram.com/",
          accentColorHex = 0xFF8B5CF6L,
          userAgentMode = UserAgentMode.ANDROID_MOBILE.name,
          privacyTrackerBlock = true,
          spoofDeviceFingerprint = true,
          quickNotes = "Design portfolio & Reels account • Tag set: #uiux #motion #androiddev",
          cookieJarJson = """{"ig_cb":"1","ig_did":"B74E-104F-002","csrftoken":"ts_inst2_44b"}""",
          localStorageJson = """{"ig_theme":"dark","creator_mode":"enabled","sandbox_slot":"instance_2"}""",
          cookieCount = 3,
          storageKeyCount = 3,
          blockedTrackersCount = 9,
          launchCount = 8,
          isPinnedFavorite = true,
          createdAt = now - 86_400_000L * 4,
          lastActiveAt = now - 240_000L,
        ),
        ClonedInstanceEntity(
          platformId = "x_twitter",
          platformName = "X (Twitter)",
          monogram = "X",
          instanceName = "Main Tech Feed",
          badgeLabel = "#1",
          accountHandle = "@alex_builds",
          workspaceGroup = "Creator",
          baseUrl = "https://x.com/",
          lastVisitedUrl = "https://x.com/",
          accentColorHex = 0xFF00F0FFL,
          userAgentMode = UserAgentMode.ANDROID_MOBILE.name,
          privacyTrackerBlock = true,
          spoofDeviceFingerprint = true,
          quickNotes = "Public engineering & product updates handle",
          cookieJarJson = """{"guest_id":"v1%3A170001","night_mode":"2"}""",
          localStorageJson = """{"tw_sandbox":"slot_1"}""",
          cookieCount = 2,
          storageKeyCount = 1,
          blockedTrackersCount = 21,
          launchCount = 15,
          isPinnedFavorite = true,
          createdAt = now - 86_400_000L * 3,
          lastActiveAt = now - 360_000L,
        ),
        ClonedInstanceEntity(
          platformId = "x_twitter",
          platformName = "X (Twitter)",
          monogram = "X",
          instanceName = "Research & Anon",
          badgeLabel = "ALT",
          accountHandle = "@cipher_observer",
          workspaceGroup = "Privacy Vault",
          baseUrl = "https://x.com/",
          lastVisitedUrl = "https://x.com/",
          accentColorHex = 0xFF10B981L,
          userAgentMode = UserAgentMode.DESKTOP_CHROME.name,
          privacyTrackerBlock = true,
          spoofDeviceFingerprint = true,
          incognitoOnExit = false,
          quickNotes = "Isolated research feed with Desktop UA & tracker stripping",
          cookieJarJson = """{"guest_id":"v1%3A998822","night_mode":"2"}""",
          localStorageJson = """{"tw_sandbox":"slot_alt"}""",
          cookieCount = 2,
          storageKeyCount = 1,
          blockedTrackersCount = 32,
          launchCount = 6,
          isPinnedFavorite = false,
          createdAt = now - 86_400_000L * 2,
          lastActiveAt = now - 600_000L,
        ),
        ClonedInstanceEntity(
          platformId = "reddit",
          platformName = "Reddit",
          monogram = "RD",
          instanceName = "Dev Communities",
          badgeLabel = "#1",
          accountHandle = "u/kotlin_architect",
          workspaceGroup = "Personal",
          baseUrl = "https://www.reddit.com/",
          lastVisitedUrl = "https://www.reddit.com/",
          accentColorHex = 0xFFF59E0BL,
          userAgentMode = UserAgentMode.ANDROID_MOBILE.name,
          privacyTrackerBlock = true,
          spoofDeviceFingerprint = true,
          quickNotes = "Subscribed: r/androiddev, r/kotlin, r/ selfhosted",
          cookieJarJson = """{"csv":"2","edgebucket":"sandbox_rd_1"}""",
          localStorageJson = """{"reddit_FeedSort":"hot"}""",
          cookieCount = 2,
          storageKeyCount = 1,
          blockedTrackersCount = 11,
          launchCount = 9,
          isPinnedFavorite = false,
          createdAt = now - 86_400_000L * 2,
          lastActiveAt = now - 900_000L,
        ),
        ClonedInstanceEntity(
          platformId = "reddit",
          platformName = "Reddit",
          monogram = "RD",
          instanceName = "Throwaway Vault",
          badgeLabel = "#2",
          accountHandle = "u/anon_throwaway_99",
          workspaceGroup = "Privacy Vault",
          baseUrl = "https://www.reddit.com/",
          lastVisitedUrl = "https://www.reddit.com/",
          accentColorHex = 0xFFEC4899L,
          userAgentMode = UserAgentMode.IOS_SAFARI.name,
          privacyTrackerBlock = true,
          spoofDeviceFingerprint = true,
          pinCode = "1234",
          quickNotes = "PIN-locked throwaway Reddit persona (Default PIN: 1234)",
          cookieJarJson = """{"csv":"2","edgebucket":"sandbox_rd_2"}""",
          localStorageJson = """{"reddit_FeedSort":"new"}""",
          cookieCount = 2,
          storageKeyCount = 1,
          blockedTrackersCount = 18,
          launchCount = 4,
          isPinnedFavorite = false,
          createdAt = now - 86_400_000L,
          lastActiveAt = now - 1_500_000L,
        ),
        ClonedInstanceEntity(
          platformId = "linkedin",
          platformName = "LinkedIn",
          monogram = "IN",
          instanceName = "Company Page Admin",
          badgeLabel = "BIZ",
          accountHandle = "@TwinSpaceHQ",
          workspaceGroup = "Business",
          baseUrl = "https://www.linkedin.com/",
          lastVisitedUrl = "https://www.linkedin.com/",
          accentColorHex = 0xFF3B82F6L,
          userAgentMode = UserAgentMode.DESKTOP_CHROME.name,
          privacyTrackerBlock = true,
          spoofDeviceFingerprint = true,
          quickNotes = "Recruiting & B2B outreach profile",
          cookieJarJson = """{"lang":"v=2&lang=en-us"}""",
          localStorageJson = """{"li_theme":"dark"}""",
          cookieCount = 1,
          storageKeyCount = 1,
          blockedTrackersCount = 7,
          launchCount = 5,
          isPinnedFavorite = false,
          createdAt = now - 43_200_000L,
          lastActiveAt = now - 2_000_000L,
        ),
      )
      dao.insertAll(starterClones)
    }
  }
}
