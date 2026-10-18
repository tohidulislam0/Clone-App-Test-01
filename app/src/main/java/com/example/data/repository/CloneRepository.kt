package com.example.data.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.os.Build
import com.example.MainActivity
import com.example.data.local.ClonedInstanceDao
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.InstalledAppInfo
import com.example.data.model.SocialPlatformCatalog
import com.example.ui.theme.InstanceColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CloneRepository(
  private val dao: ClonedInstanceDao,
  private val appContext: Context,
) {
  val allInstances: Flow<List<ClonedInstanceEntity>> = dao.observeAllInstances()

  suspend fun getInstanceById(id: Long): ClonedInstanceEntity? = dao.getInstanceById(id)

  suspend fun createClone(instance: ClonedInstanceEntity): Long {
    return dao.insertInstance(instance)
  }

  suspend fun duplicateExistingInstance(source: ClonedInstanceEntity): ClonedInstanceEntity {
    val existingCount = dao.getInstanceCountForPlatform(source.platformId)
    val nextNumber = existingCount + 1
    val nextColor = InstanceColors[nextNumber % InstanceColors.size]
    val copy = source.copy(
      id = 0L,
      instanceName = "${source.platformName} Clone #$nextNumber",
      badgeLabel = "#$nextNumber",
      accountHandle = "@account_$nextNumber",
      lastVisitedUrl = source.baseUrl,
      accentColorHex = nextColor,
      cookieJarJson = "{}",
      localStorageJson = "{}",
      sessionStorageJson = "{}",
      cookieCount = 0,
      storageKeyCount = 0,
      blockedTrackersCount = 0,
      launchCount = 0,
      pinCode = "",
      createdAt = System.currentTimeMillis(),
      lastActiveAt = System.currentTimeMillis(),
    )
    val newId = dao.insertInstance(copy)
    return copy.copy(id = newId)
  }

  suspend fun updateInstance(instance: ClonedInstanceEntity) {
    dao.updateInstance(instance)
  }

  suspend fun deleteInstance(id: Long) {
    dao.deleteInstanceById(id)
  }

  suspend fun saveSessionSnapshot(
    id: Long,
    url: String,
    cookieJarJson: String,
    localStorageJson: String,
    sessionStorageJson: String,
    cookieCount: Int,
    storageKeyCount: Int,
    blockedTrackersCount: Int,
  ) {
    dao.updateSessionSnapshot(
      id = id,
      url = url,
      cookieJarJson = cookieJarJson,
      localStorageJson = localStorageJson,
      sessionStorageJson = sessionStorageJson,
      cookieCount = cookieCount,
      storageKeyCount = storageKeyCount,
      blockedTrackersCount = blockedTrackersCount,
    )
  }

  suspend fun wipeInstanceSession(id: Long) {
    dao.clearInstanceSession(id)
  }

  suspend fun recordLaunch(id: Long) {
    dao.incrementLaunchCount(id)
  }

  suspend fun scanDeviceInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
    val pm = appContext.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
      } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(mainIntent, 0)
      }
    } catch (_: Exception) {
      emptyList()
    }

    val seenPackages = mutableSetOf<String>()
    val results = mutableListOf<InstalledAppInfo>()

    for (info in resolveInfos) {
      val pkg = info.activityInfo?.packageName ?: continue
      if (pkg == appContext.packageName || !seenPackages.add(pkg)) continue
      val appInfo = info.activityInfo?.applicationInfo
      val isSystemApp = appInfo != null &&
        (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

      val label = try {
        info.loadLabel(pm).toString().ifBlank { pkg }
      } catch (_: Exception) {
        pkg
      }

      val matched = SocialPlatformCatalog.platforms.find { platform ->
        platform.knownPackageNames.any { it.equals(pkg, ignoreCase = true) } ||
          label.contains(platform.name, ignoreCase = true)
      }

      // Include user-installed apps, matched social apps, or recognizable communication/browser apps
      if (!isSystemApp || matched != null || isSocialOrBrowserLabel(label, pkg)) {
        val fallbackUrl = matched?.defaultUrl ?: inferWebUrlForApp(label, pkg)
        results.add(
          InstalledAppInfo(
            packageName = pkg,
            appLabel = label,
            matchedPlatform = matched,
            fallbackWebUrl = fallbackUrl,
          )
        )
      }
    }

    results.sortedWith(
      compareByDescending<InstalledAppInfo> { it.matchedPlatform != null }
        .thenBy { it.appLabel.lowercase() }
    )
  }

  private fun isSocialOrBrowserLabel(label: String, pkg: String): Boolean {
    val keywords = listOf(
      "social", "chat", "messenger", "chrome", "browser", "youtube", "gmail", "mail", "messages", "photos"
    )
    return keywords.any { label.contains(it, ignoreCase = true) || pkg.contains(it, ignoreCase = true) }
  }

  private fun inferWebUrlForApp(label: String, pkg: String): String {
    val clean = label.lowercase().trim()
    return when {
      clean.contains("youtube") -> "https://m.youtube.com/"
      clean.contains("chrome") || clean.contains("browser") -> "https://duckduckgo.com/"
      clean.contains("gmail") || clean.contains("mail") -> "https://mail.google.com/"
      else -> {
        val query = java.net.URLEncoder.encode("$label login", "UTF-8")
        "https://duckduckgo.com/?q=$query"
      }
    }
  }

  fun pinInstanceShortcutToLauncher(instance: ClonedInstanceEntity): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    val shortcutManager = appContext.getSystemService(ShortcutManager::class.java) ?: return false
    if (!shortcutManager.isRequestPinShortcutSupported) return false

    val launchIntent = Intent(appContext, MainActivity::class.java).apply {
      action = Intent.ACTION_VIEW
      putExtra("EXTRA_LAUNCH_INSTANCE_ID", instance.id)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val badgeBitmap = renderInstanceShortcutBitmap(instance)
    val shortcutInfo = ShortcutInfo.Builder(appContext, "twinspace_clone_${instance.id}")
      .setShortLabel("${instance.badgeLabel} ${instance.instanceName}".take(16))
      .setLongLabel("${instance.platformName} • ${instance.instanceName}")
      .setIcon(Icon.createWithBitmap(badgeBitmap))
      .setIntent(launchIntent)
      .build()

    val pinnedCallback = PendingIntent.getBroadcast(
      appContext,
      instance.id.toInt(),
      Intent("com.aistudio.twinspace.SHORTCUT_PINNED"),
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    return try {
      shortcutManager.requestPinShortcut(shortcutInfo, pinnedCallback.intentSender)
    } catch (_: Exception) {
      false
    }
  }

  private fun renderInstanceShortcutBitmap(instance: ClonedInstanceEntity): Bitmap {
    val size = 192
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background rounded square
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF111726.toInt()
    }
    canvas.drawRoundRect(RectF(8f, 8f, size - 8f, size - 8f), 44f, 44f, bgPaint)

    // Accent border ring
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 8f
      color = (instance.accentColorHex or 0xFF000000L).toInt()
    }
    canvas.drawRoundRect(RectF(14f, 14f, size - 14f, size - 14f), 38f, 38f, borderPaint)

    // Monogram text
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFFF3F6FC.toInt()
      textSize = 68f
      textAlign = Paint.Align.CENTER
      isFakeBoldText = true
    }
    canvas.drawText(instance.monogram.take(2), size / 2f, size / 2f + 22f, textPaint)

    // Badge pill in bottom-right corner
    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = (instance.accentColorHex or 0xFF000000L).toInt()
    }
    canvas.drawRoundRect(RectF(108f, 124f, 180f, 176f), 20f, 20f, badgePaint)

    val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF090D16.toInt()
      textSize = 28f
      textAlign = Paint.Align.CENTER
      isFakeBoldText = true
    }
    canvas.drawText(instance.badgeLabel.take(3), 144f, 159f, badgeTextPaint)

    return bitmap
  }
}
