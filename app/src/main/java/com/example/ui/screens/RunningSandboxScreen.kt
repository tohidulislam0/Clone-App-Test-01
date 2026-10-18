package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.UserAgentMode
import com.example.sandbox.SessionSandboxEngine
import com.example.sandbox.TwinSpaceJsBridge
import com.example.sandbox.triggerSandboxSnapshotSync
import com.example.ui.components.InstanceAvatarBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismViolet
import java.io.ByteArrayInputStream
import java.util.UUID

@Composable
fun RunningSandboxScreen(
  allInstances: List<ClonedInstanceEntity>,
  primaryInstance: ClonedInstanceEntity?,
  secondaryInstance: ClonedInstanceEntity?,
  isDualSplitMode: Boolean,
  onSelectPrimaryInstance: (ClonedInstanceEntity) -> Unit,
  onSelectSecondaryInstance: (ClonedInstanceEntity) -> Unit,
  onToggleDualSplitMode: () -> Unit,
  onSwapPanes: () -> Unit,
  onPersistWebSession: (
    instance: ClonedInstanceEntity,
    currentUrl: String,
    localStorageJson: String?,
    sessionStorageJson: String?,
    storageKeyCount: Int?,
    additionalBlockedTrackers: Int,
  ) -> Unit,
  onWipeSession: (ClonedInstanceEntity) -> Unit,
  onBackToWorkspace: () -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler {
    onBackToWorkspace()
  }

  val activePrimary = primaryInstance ?: allInstances.firstOrNull()

  if (activePrimary == null) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "No Active Cloned Instance Selected",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Button(
          onClick = onBackToWorkspace,
          colors = ButtonDefaults.buttonColors(
            containerColor = ElectricCyan,
            contentColor = ObsidianBg,
          ),
        ) {
          Text("Return to Workspace")
        }
      }
    }
    return
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Fast Multi-Instance Switcher Dock + Dual Split Toggle Bar
    Surface(
      color = ObsidianSurface,
      tonalElevation = 4.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          IconButton(
            onClick = onBackToWorkspace,
            modifier = Modifier
              .size(48.dp)
              .testTag("sandbox_back_to_workspace_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Workspace",
              tint = ElectricCyan,
            )
          }

          LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
          ) {
            items(allInstances, key = { it.id }) { item ->
              val isPrimary = item.id == activePrimary.id
              val isSecondary = isDualSplitMode && item.id == secondaryInstance?.id
              val accent = Color(item.accentColorHex or 0xFF000000L)

              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    when {
                      isPrimary -> accent.copy(alpha = 0.24f)
                      isSecondary -> PrismViolet.copy(alpha = 0.24f)
                      else -> ObsidianSurfaceElevated
                    }
                  )
                  .border(
                    width = if (isPrimary || isSecondary) 1.5.dp else 1.dp,
                    color = when {
                      isPrimary -> accent
                      isSecondary -> PrismViolet
                      else -> MaterialTheme.colorScheme.outline
                    },
                    shape = RoundedCornerShape(12.dp),
                  )
                  .clickable {
                    if (isDualSplitMode && !isPrimary) {
                      onSelectSecondaryInstance(item)
                    } else {
                      onSelectPrimaryInstance(item)
                    }
                  }
                  .padding(horizontal = 10.dp, vertical = 8.dp)
                  .testTag("switcher_pill_${item.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Surface(
                  color = accent,
                  shape = RoundedCornerShape(4.dp),
                ) {
                  Text(
                    text = item.badgeLabel,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = ObsidianBg,
                    fontWeight = FontWeight.Bold,
                  )
                }
                Text(
                  text = "${item.monogram} • ${item.instanceName}",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White,
                  maxLines = 1,
                )
              }
            }
          }

          if (isDualSplitMode) {
            IconButton(
              onClick = onSwapPanes,
              modifier = Modifier
                .size(48.dp)
                .testTag("swap_split_panes_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.SwapCalls,
                contentDescription = "Swap Primary and Secondary Panes",
                tint = PrismViolet,
              )
            }
          }

          IconButton(
            onClick = onToggleDualSplitMode,
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isDualSplitMode) PrismViolet.copy(alpha = 0.25f) else ObsidianSurfaceElevated)
              .testTag("toggle_dual_split_mode_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.VerticalSplit,
              contentDescription = "Toggle Dual Split-Screen Parallel View",
              tint = if (isDualSplitMode) PrismViolet else ElectricCyan,
            )
          }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
      }
    }

    // Main Sandbox Execution Area (Single or Adaptive Dual Split-Screen)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val isWideScreen = maxWidth >= 700.dp
      val activeSecondary = secondaryInstance
        ?: allInstances.firstOrNull { it.id != activePrimary.id }
        ?: activePrimary

      if (!isDualSplitMode) {
        key(activePrimary.id) {
          IsolatedSandboxPane(
            instance = activePrimary,
            paneTag = "PRIMARY SANDBOX",
            onPersistWebSession = onPersistWebSession,
            onWipeSession = onWipeSession,
            onShowToast = onShowToast,
            modifier = Modifier.fillMaxSize(),
          )
        }
      } else if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
          key("wide_primary_${activePrimary.id}") {
            IsolatedSandboxPane(
              instance = activePrimary,
              paneTag = "PANE A",
              onPersistWebSession = onPersistWebSession,
              onWipeSession = onWipeSession,
              onShowToast = onShowToast,
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            )
          }
          VerticalDivider(thickness = 2.dp, color = PrismViolet)
          key("wide_secondary_${activeSecondary.id}") {
            IsolatedSandboxPane(
              instance = activeSecondary,
              paneTag = "PANE B",
              onPersistWebSession = onPersistWebSession,
              onWipeSession = onWipeSession,
              onShowToast = onShowToast,
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            )
          }
        }
      } else {
        Column(modifier = Modifier.fillMaxSize()) {
          key("tall_primary_${activePrimary.id}") {
            IsolatedSandboxPane(
              instance = activePrimary,
              paneTag = "TOP CLONE",
              onPersistWebSession = onPersistWebSession,
              onWipeSession = onWipeSession,
              onShowToast = onShowToast,
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            )
          }
          HorizontalDivider(thickness = 2.dp, color = PrismViolet)
          key("tall_secondary_${activeSecondary.id}") {
            IsolatedSandboxPane(
              instance = activeSecondary,
              paneTag = "BOTTOM CLONE",
              onPersistWebSession = onPersistWebSession,
              onWipeSession = onWipeSession,
              onShowToast = onShowToast,
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            )
          }
        }
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun IsolatedSandboxPane(
  instance: ClonedInstanceEntity,
  paneTag: String,
  onPersistWebSession: (
    instance: ClonedInstanceEntity,
    currentUrl: String,
    localStorageJson: String?,
    sessionStorageJson: String?,
    storageKeyCount: Int?,
    additionalBlockedTrackers: Int,
  ) -> Unit,
  onWipeSession: (ClonedInstanceEntity) -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val accentColor = Color(instance.accentColorHex or 0xFF000000L)
  val clipboardManager = LocalClipboardManager.current

  var currentUrl by remember(instance.id) { mutableStateOf(instance.lastVisitedUrl.ifBlank { instance.baseUrl }) }
  var useLiveWebView by remember(instance.id) { mutableStateOf(false) }
  var isLoading by remember(instance.id) { mutableStateOf(false) }
  var showVaultBar by remember(instance.id) { mutableStateOf(false) }
  var webViewRef by remember(instance.id) { mutableStateOf<WebView?>(null) }
  var blockedInSession by remember(instance.id) { mutableIntStateOf(0) }

  Column(modifier = modifier.background(ObsidianBg)) {
    // Compact Sandbox Header Bar for this specific Pane
    Surface(
      color = ObsidianSurfaceElevated,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            color = accentColor,
            shape = RoundedCornerShape(6.dp),
          ) {
            Text(
              text = "${instance.badgeLabel} • $paneTag",
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall,
              color = ObsidianBg,
              fontWeight = FontWeight.Bold,
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${instance.platformName} — ${instance.instanceName} (${instance.accountHandle})",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${instance.sandboxNamespace} • ${instance.deviceFingerprintHash} • $currentUrl",
              style = MaterialTheme.typography.labelSmall,
              color = NeonEmerald,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          // Toggle between Native Sandbox Container & Software-Layered WebView
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                useLiveWebView = !useLiveWebView
              }
              .testTag("pane_mode_toggle_${instance.id}"),
            color = if (useLiveWebView) PrismViolet.copy(alpha = 0.25f) else ElectricCyan.copy(alpha = 0.16f),
            shape = RoundedCornerShape(8.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Language,
                contentDescription = "Toggle Web Engine",
                tint = if (useLiveWebView) PrismViolet else ElectricCyan,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = if (useLiveWebView) "Web ON" else "Sandbox UI",
                style = MaterialTheme.typography.labelSmall,
                color = if (useLiveWebView) Color.White else ElectricCyan,
              )
            }
          }

          IconButton(
            onClick = { showVaultBar = !showVaultBar },
            modifier = Modifier
              .size(40.dp)
              .testTag("pane_vault_button_${instance.id}"),
          ) {
            Icon(
              imageVector = Icons.Filled.Key,
              contentDescription = "Quick Account Handle & Notes Vault",
              tint = if (showVaultBar) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }

          IconButton(
            onClick = {
              onWipeSession(instance)
              webViewRef?.clearCache(true)
            },
            modifier = Modifier
              .size(40.dp)
              .testTag("pane_wipe_button_${instance.id}"),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteSweep,
              contentDescription = "Wipe Isolated Session Cookies",
              tint = Color(0xFFEF4444),
              modifier = Modifier.size(18.dp),
            )
          }
        }

        // Expandable Account Handle & Quick-Paste Vault Drawer
        AnimatedVisibility(visible = showVaultBar) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(ObsidianBg)
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Security,
              contentDescription = null,
              tint = ElectricCyan,
              modifier = Modifier.size(16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Handle: ${instance.accountHandle}",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan,
              )
              if (instance.quickNotes.isNotBlank()) {
                Text(
                  text = instance.quickNotes,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
            Button(
              onClick = {
                clipboardManager.setText(AnnotatedString(instance.accountHandle.removePrefix("@")))
                onShowToast("Copied handle '${instance.accountHandle.removePrefix("@")}' to clipboard")
              },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(36.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                contentColor = ObsidianBg,
              ),
            ) {
              Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Copy Handle", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        if (isLoading && useLiveWebView) {
          LinearProgressIndicator(
            modifier = Modifier
              .fillMaxWidth()
              .height(2.dp),
            color = accentColor,
            trackColor = ObsidianBg,
          )
        }
      }
    }

    if (!useLiveWebView) {
      NativeIsolatedSocialContainer(
        instance = instance,
        accentColor = accentColor,
        onPersistSession = { updatedCookiesMap, updatedStorageMap, blockedDelta ->
          val cookiesJson = SessionSandboxEngine.encodeJsonMap(updatedCookiesMap)
          val storageJson = SessionSandboxEngine.encodeJsonMap(updatedStorageMap)
          onPersistWebSession(
            instance.copy(
              cookieJarJson = cookiesJson,
              cookieCount = updatedCookiesMap.size,
            ),
            currentUrl,
            storageJson,
            instance.sessionStorageJson,
            updatedStorageMap.size,
            blockedDelta,
          )
        },
        onSwitchToLiveWeb = { useLiveWebView = true },
        onShowToast = onShowToast,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      // Software-layered Android WebView Sandbox (avoids hardware DRM / MESA rendernode errors)
      AndroidView(
        modifier = Modifier
          .fillMaxSize()
          .testTag("sandbox_webview_${instance.id}"),
        factory = { context ->
          WebView(context).apply {
            webViewRef = this
            // Force software layer rendering so Chromium never queries missing MESA /dev/dri/renderD128
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            val uaMode = UserAgentMode.fromName(instance.userAgentMode)
            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              databaseEnabled = true
              userAgentString = uaMode.userAgentString
              useWideViewPort = true
              loadWithOverviewMode = true
              builtInZoomControls = true
              displayZoomControls = false
              mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            SessionSandboxEngine.applyIsolatedCookiesForInstance(
              instance = instance,
              syncWithSystemWebView = true,
            )

            addJavascriptInterface(
              TwinSpaceJsBridge { localStorageJson, sessionStorageJson, totalKeys ->
                onPersistWebSession(
                  instance,
                  currentUrl,
                  localStorageJson,
                  sessionStorageJson,
                  totalKeys,
                  blockedInSession,
                )
                blockedInSession = 0
              },
              "TwinSpaceBridge",
            )

            webViewClient = object : WebViewClient() {
              override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?,
              ): WebResourceResponse? {
                val reqUrl = request?.url?.toString().orEmpty()
                if (instance.privacyTrackerBlock && SessionSandboxEngine.isTrackerUrl(reqUrl)) {
                  blockedInSession++
                  return WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    ByteArrayInputStream(ByteArray(0)),
                  )
                }
                return super.shouldInterceptRequest(view, request)
              }

              override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
              ): Boolean {
                val rawUrl = request?.url?.toString() ?: return false
                if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                  return true
                }
                val cleanUrl = SessionSandboxEngine.sanitizeTrackingParams(
                  url = rawUrl,
                  enabled = instance.privacyTrackerBlock,
                )
                if (cleanUrl != rawUrl) {
                  view?.loadUrl(cleanUrl)
                  return true
                }
                return false
              }

              override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isLoading = true
                if (!url.isNullOrBlank()) {
                  currentUrl = url
                }
                view?.triggerSandboxSnapshotSync(instance)
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading = false
                val finalUrl = url ?: currentUrl
                currentUrl = finalUrl
                view?.triggerSandboxSnapshotSync(instance)
                onPersistWebSession(
                  instance,
                  finalUrl,
                  null,
                  null,
                  null,
                  blockedInSession,
                )
                blockedInSession = 0
              }

              override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?,
              ): Boolean {
                // Gracefully recover and switch back to native sandbox container if WebView renderer exits
                useLiveWebView = false
                return true
              }
            }

            loadUrl(instance.lastVisitedUrl.ifBlank { instance.baseUrl })
          }
        },
      )
    }

    DisposableEffect(instance.id) {
      onDispose {
        if (instance.incognitoOnExit) {
          onWipeSession(instance)
        }
        webViewRef?.stopLoading()
      }
    }
  }
}

@Composable
private fun NativeIsolatedSocialContainer(
  instance: ClonedInstanceEntity,
  accentColor: Color,
  onPersistSession: (
    updatedCookies: Map<String, String>,
    updatedStorage: Map<String, String>,
    blockedDelta: Int,
  ) -> Unit,
  onSwitchToLiveWeb: () -> Unit,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val uaMode = UserAgentMode.fromName(instance.userAgentMode)
  var cookieMap by remember(instance.id, instance.cookieJarJson) {
    mutableStateOf(SessionSandboxEngine.parseJsonMap(instance.cookieJarJson))
  }
  var storageMap by remember(instance.id, instance.localStorageJson) {
    mutableStateOf(SessionSandboxEngine.parseJsonMap(instance.localStorageJson))
  }
  var draftPostText by remember(instance.id) { mutableStateOf("") }
  var newCookieKey by remember(instance.id) { mutableStateOf("") }
  var newCookieValue by remember(instance.id) { mutableStateOf("") }

  val postsInSandbox = remember(storageMap) {
    storageMap.entries
      .filter { it.key.startsWith("post_") }
      .sortedByDescending { it.key }
      .map { it.value }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("native_sandbox_container_${instance.id}"),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Active Account Session Card inside this isolated container
    item(key = "account_session_header") {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor.copy(alpha = 0.55f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            InstanceAvatarBadge(
              monogram = instance.monogram,
              badgeLabel = instance.badgeLabel,
              accentColorHex = instance.accentColorHex,
              hasPinLock = instance.hasPinLock,
              size = 48.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "${instance.platformName} (${instance.badgeLabel})",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                  imageVector = Icons.Filled.VerifiedUser,
                  contentDescription = "Isolated session verified",
                  tint = NeonEmerald,
                  modifier = Modifier.size(16.dp),
                )
              }
              Text(
                text = "Signed in as ${instance.accountHandle} • Group: ${instance.workspaceGroup}",
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
              )
              Text(
                text = "Container ID: ${instance.sandboxNamespace} | Fingerprint: ${instance.deviceFingerprintHash}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          // Quick Actions inside the running clone
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = {
                val rotatedToken = "ts_tok_${UUID.randomUUID().toString().take(8)}"
                val nextCookies = cookieMap.toMutableMap().apply {
                  put("session_id_${instance.id}", rotatedToken)
                }
                cookieMap = nextCookies
                onPersistSession(nextCookies, storageMap, 2)
                onShowToast("Rotated isolated session token for ${instance.instanceName}")
              },
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("rotate_token_button_${instance.id}"),
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Rotate Token", style = MaterialTheme.typography.labelMedium)
            }

            Button(
              onClick = onSwitchToLiveWeb,
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("open_live_web_button_${instance.id}"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = ObsidianBg,
              ),
            ) {
              Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Open Live Web", style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }

    // Per-Instance Post / Status Composer (persisted strictly inside this clone's isolated storage!)
    item(key = "sandbox_post_composer") {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Post or Save Draft as ${instance.accountHandle} (Isolated to ${instance.badgeLabel})",
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = draftPostText,
              onValueChange = { draftPostText = it },
              placeholder = {
                Text(
                  text = "Write a post or status in ${instance.instanceName}...",
                  style = MaterialTheme.typography.bodySmall,
                )
              },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("sandbox_post_input_${instance.id}"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
              ),
            )
            Button(
              onClick = {
                if (draftPostText.isNotBlank()) {
                  val key = "post_${System.currentTimeMillis()}"
                  val nextStorage = storageMap.toMutableMap().apply {
                    put(key, draftPostText.trim())
                  }
                  storageMap = nextStorage
                  draftPostText = ""
                  onPersistSession(cookieMap, nextStorage, 1)
                  onShowToast("Published to ${instance.instanceName} (${instance.badgeLabel}) isolated storage")
                }
              },
              modifier = Modifier
                .height(48.dp)
                .testTag("sandbox_publish_button_${instance.id}"),
              colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = ObsidianBg,
              ),
            ) {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Publish to instance", modifier = Modifier.size(16.dp))
            }
          }

          if (postsInSandbox.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            postsInSandbox.forEachIndexed { idx, postContent ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(ObsidianBg)
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${instance.accountHandle} • ${instance.badgeLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                  )
                  Text(
                    text = postContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
        }
      }
    }

    // Isolated CookieJar & DOM Storage Partition Inspector for this Instance
    item(key = "isolated_cookie_vault") {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Storage,
              contentDescription = null,
              tint = NeonEmerald,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = "ISOLATED COOKIE JAR (${cookieMap.size}) & DOM STORAGE (${storageMap.size})",
              style = MaterialTheme.typography.labelLarge,
              color = NeonEmerald,
            )
          }

          cookieMap.entries.forEach { (k, v) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianBg)
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "cookie: $k",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan,
              )
              Text(
                text = v,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          // Add custom session cookie to test isolation between Clone #1 and Clone #2
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = newCookieKey,
              onValueChange = { newCookieKey = it },
              placeholder = { Text("Cookie key", style = MaterialTheme.typography.bodySmall) },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = newCookieValue,
              onValueChange = { newCookieValue = it },
              placeholder = { Text("Value", style = MaterialTheme.typography.bodySmall) },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            IconButton(
              onClick = {
                if (newCookieKey.isNotBlank()) {
                  val nextCookies = cookieMap.toMutableMap().apply {
                    put(newCookieKey.trim(), newCookieValue.trim().ifBlank { "1" })
                  }
                  cookieMap = nextCookies
                  newCookieKey = ""
                  newCookieValue = ""
                  onPersistSession(nextCookies, storageMap, 0)
                  onShowToast("Saved isolated cookie to ${instance.sandboxNamespace}")
                }
              },
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeonEmerald.copy(alpha = 0.2f))
                .testTag("add_cookie_button_${instance.id}"),
            ) {
              Icon(Icons.Filled.Add, contentDescription = "Add isolated cookie", tint = NeonEmerald)
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Fingerprint,
              contentDescription = null,
              tint = PrismViolet,
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = "Spoofed UA: ${uaMode.label} • Trackers Blocked: ${instance.blockedTrackersCount}",
              style = MaterialTheme.typography.labelSmall,
              color = PrismViolet,
            )
          }
        }
      }
    }
  }
}
