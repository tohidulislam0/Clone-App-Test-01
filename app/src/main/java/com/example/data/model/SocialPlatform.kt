package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserAgentMode(val label: String, val shortTag: String, val userAgentString: String) {
  ANDROID_MOBILE(
    label = "Android Mobile (Pixel 9 Pro)",
    shortTag = "MOBILE",
    userAgentString = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
  ),
  DESKTOP_CHROME(
    label = "Desktop Workstation (Chrome 131)",
    shortTag = "DESKTOP",
    userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
  ),
  IOS_SAFARI(
    label = "iOS Mobile (iPhone 16 Pro)",
    shortTag = "iOS",
    userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1",
  ),
  TABLET_FOLD(
    label = "Android Tablet / Foldable",
    shortTag = "TABLET",
    userAgentString = "Mozilla/5.0 (Linux; Android 15; Pixel Pro Fold) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
  );

  companion object {
    fun fromName(name: String): UserAgentMode = entries.find { it.name == name } ?: ANDROID_MOBILE
  }
}

data class SocialPlatform(
  val id: String,
  val name: String,
  val category: String,
  val defaultUrl: String,
  val domainHost: String,
  val brandColorHex: Long,
  val monogram: String,
  val description: String,
  val knownPackageNames: List<String> = emptyList(),
)

@Entity(tableName = "cloned_instances")
data class ClonedInstanceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val platformId: String,
  val platformName: String,
  val monogram: String,
  val instanceName: String,
  val badgeLabel: String,
  val accountHandle: String,
  val workspaceGroup: String,
  val baseUrl: String,
  val lastVisitedUrl: String,
  val accentColorHex: Long,
  val userAgentMode: String = UserAgentMode.ANDROID_MOBILE.name,
  val privacyTrackerBlock: Boolean = true,
  val spoofDeviceFingerprint: Boolean = true,
  val forceDarkWebMode: Boolean = false,
  val incognitoOnExit: Boolean = false,
  val pinCode: String = "",
  val quickNotes: String = "",
  val cookieJarJson: String = "{}",
  val localStorageJson: String = "{}",
  val sessionStorageJson: String = "{}",
  val cookieCount: Int = 0,
  val storageKeyCount: Int = 0,
  val blockedTrackersCount: Int = 0,
  val launchCount: Int = 0,
  val isPinnedFavorite: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val lastActiveAt: Long = System.currentTimeMillis(),
) {
  val hasPinLock: Boolean
    get() = pinCode.length == 4

  val sandboxNamespace: String
    get() = "ns_${platformId.lowercase()}_#${id.toString().padStart(3, '0')}"

  val deviceFingerprintHash: String
    get() {
      val raw = ((id + 17L) * 0x45D9F3BL) xor platformId.hashCode().toLong()
      return "FP-" + java.lang.Long.toHexString(raw and 0xFFFFFFF).uppercase().padStart(7, '0')
    }
}

data class InstalledAppInfo(
  val packageName: String,
  val appLabel: String,
  val matchedPlatform: SocialPlatform?,
  val fallbackWebUrl: String,
)

object SocialPlatformCatalog {
  val workspaceGroups = listOf("All", "Personal", "Creator", "Business", "Privacy Vault")

  val platforms = listOf(
    SocialPlatform(
      id = "instagram",
      name = "Instagram",
      category = "Photo & Reels",
      defaultUrl = "https://www.instagram.com/accounts/login/",
      domainHost = "instagram.com",
      brandColorHex = 0xFFE1306CL,
      monogram = "IG",
      description = "Run personal, creator studio, and business storefronts in isolated cookie vaults.",
      knownPackageNames = listOf("com.instagram.android", "com.instagram.lite"),
    ),
    SocialPlatform(
      id = "x_twitter",
      name = "X (Twitter)",
      category = "Microblogging",
      defaultUrl = "https://x.com/i/flow/login",
      domainHost = "x.com",
      brandColorHex = 0xFF00F0FFL,
      monogram = "X",
      description = "Switch between main, tech community, and anonymous feeds simultaneously.",
      knownPackageNames = listOf("com.twitter.android", "com.twitter.android.lite"),
    ),
    SocialPlatform(
      id = "reddit",
      name = "Reddit",
      category = "Communities",
      defaultUrl = "https://www.reddit.com/login/",
      domainHost = "reddit.com",
      brandColorHex = 0xFFFF5700L,
      monogram = "RD",
      description = "Separate hobby subreddits, professional accounts, and throwaway personas.",
      knownPackageNames = listOf("com.reddit.frontpage"),
    ),
    SocialPlatform(
      id = "facebook",
      name = "Facebook",
      category = "Social Network",
      defaultUrl = "https://m.facebook.com/login/",
      domainHost = "facebook.com",
      brandColorHex = 0xFF1877F2L,
      monogram = "FB",
      description = "Isolate Marketplace, Page Manager, and personal family profiles with zero cross-tracking.",
      knownPackageNames = listOf("com.facebook.katana", "com.facebook.lite"),
    ),
    SocialPlatform(
      id = "tiktok",
      name = "TikTok",
      category = "Short Video",
      defaultUrl = "https://www.tiktok.com/login",
      domainHost = "tiktok.com",
      brandColorHex = 0xFF00F2FEL,
      monogram = "TT",
      description = "Keep separate recommendation algorithms and creator accounts in independent sandboxes.",
      knownPackageNames = listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"),
    ),
    SocialPlatform(
      id = "linkedin",
      name = "LinkedIn",
      category = "Professional",
      defaultUrl = "https://www.linkedin.com/login",
      domainHost = "linkedin.com",
      brandColorHex = 0xFF0A66C2L,
      monogram = "IN",
      description = "Manage executive personal branding alongside recruiting or company page accounts.",
      knownPackageNames = listOf("com.linkedin.android"),
    ),
    SocialPlatform(
      id = "bluesky",
      name = "Bluesky",
      category = "Decentralized",
      defaultUrl = "https://bsky.app/",
      domainHost = "bsky.app",
      brandColorHex = 0xFF0285FFL,
      monogram = "BS",
      description = "Multi-handle AT Protocol social client with isolated localStorage vaults.",
      knownPackageNames = listOf("xyz.blueskyweb.app"),
    ),
    SocialPlatform(
      id = "threads",
      name = "Threads",
      category = "Microblogging",
      defaultUrl = "https://www.threads.net/login",
      domainHost = "threads.net",
      brandColorHex = 0xFFA78BFAL,
      monogram = "TH",
      description = "Run parallel Threads accounts without logging out of your primary profile.",
      knownPackageNames = listOf("com.instagram.barcelona"),
    ),
    SocialPlatform(
      id = "discord",
      name = "Discord Web",
      category = "Communities & Chat",
      defaultUrl = "https://discord.com/login",
      domainHost = "discord.com",
      brandColorHex = 0xFF5865F2L,
      monogram = "DC",
      description = "Stay signed into gaming servers and professional community hubs at the same time.",
      knownPackageNames = listOf("com.discord"),
    ),
    SocialPlatform(
      id = "mastodon",
      name = "Mastodon",
      category = "Fediverse",
      defaultUrl = "https://mastodon.social/auth/sign_in",
      domainHost = "mastodon.social",
      brandColorHex = 0xFF6364FFL,
      monogram = "MD",
      description = "Connect to multiple Fediverse instances simultaneously with isolated credentials.",
      knownPackageNames = listOf("org.joinmastodon.android"),
    ),
    SocialPlatform(
      id = "pinterest",
      name = "Pinterest",
      category = "Visual Discovery",
      defaultUrl = "https://www.pinterest.com/login/",
      domainHost = "pinterest.com",
      brandColorHex = 0xFFE60023L,
      monogram = "PT",
      description = "Separate client moodboards from personal design collections.",
      knownPackageNames = listOf("com.pinterest"),
    ),
    SocialPlatform(
      id = "telegram",
      name = "Telegram Web",
      category = "Messaging",
      defaultUrl = "https://web.telegram.org/k/",
      domainHost = "telegram.org",
      brandColorHex = 0xFF26A5E4L,
      monogram = "TG",
      description = "Run secondary phone number or channel admin sessions in an isolated container.",
      knownPackageNames = listOf("org.telegram.messenger"),
    ),
    SocialPlatform(
      id = "youtube",
      name = "YouTube",
      category = "Video & Channels",
      defaultUrl = "https://m.youtube.com/",
      domainHost = "youtube.com",
      brandColorHex = 0xFFFF0033L,
      monogram = "YT",
      description = "Isolate watch history and subscriptions across different Google/Brand channels.",
      knownPackageNames = listOf("com.google.android.youtube"),
    ),
    SocialPlatform(
      id = "tumblr",
      name = "Tumblr",
      category = "Blogging",
      defaultUrl = "https://www.tumblr.com/login",
      domainHost = "tumblr.com",
      brandColorHex = 0xFF00B8FFL,
      monogram = "TB",
      description = "Maintain multiple independent art, writing, and fandom blogs side by side.",
      knownPackageNames = listOf("com.tumblr"),
    ),
  )

  fun findById(id: String): SocialPlatform? = platforms.find { it.id == id }
}
