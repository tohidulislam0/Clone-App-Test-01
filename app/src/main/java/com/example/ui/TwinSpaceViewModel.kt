package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.TwinSpaceDatabase
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.InstalledAppInfo
import com.example.data.model.SocialPlatform
import com.example.data.model.UserAgentMode
import com.example.data.repository.CloneRepository
import com.example.sandbox.SessionSandboxEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TwinSpaceDestination(val route: String, val label: String) {
  WORKSPACE("workspace", "Workspace"),
  RUNNING_SANDBOX("running_sandbox", "Running"),
  CLONE_CATALOG("clone_catalog", "Clone App"),
  SECURITY_AUDIT("security_audit", "Sandbox"),
}

class TwinSpaceViewModel(
  application: Application,
  private val repository: CloneRepository,
) : AndroidViewModel(application) {

  val allInstances: StateFlow<List<ClonedInstanceEntity>> = repository.allInstances
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  private val _currentTab = MutableStateFlow(TwinSpaceDestination.WORKSPACE)
  val currentTab: StateFlow<TwinSpaceDestination> = _currentTab.asStateFlow()

  private val _selectedWorkspaceGroup = MutableStateFlow("All")
  val selectedWorkspaceGroup: StateFlow<String> = _selectedWorkspaceGroup.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _activePrimaryInstanceId = MutableStateFlow<Long?>(null)
  val activePrimaryInstanceId: StateFlow<Long?> = _activePrimaryInstanceId.asStateFlow()

  private val _activeSecondaryInstanceId = MutableStateFlow<Long?>(null)
  val activeSecondaryInstanceId: StateFlow<Long?> = _activeSecondaryInstanceId.asStateFlow()

  private val _isDualSplitMode = MutableStateFlow(false)
  val isDualSplitMode: StateFlow<Boolean> = _isDualSplitMode.asStateFlow()

  private val _unlockedInstanceIds = MutableStateFlow<Set<Long>>(emptySet())
  val unlockedInstanceIds: StateFlow<Set<Long>> = _unlockedInstanceIds.asStateFlow()

  private val _pendingPinInstance = MutableStateFlow<ClonedInstanceEntity?>(null)
  val pendingPinInstance: StateFlow<ClonedInstanceEntity?> = _pendingPinInstance.asStateFlow()

  private val _pendingPinTargetSecondary = MutableStateFlow(false)
  val pendingPinTargetSecondary: StateFlow<Boolean> = _pendingPinTargetSecondary.asStateFlow()

  private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
  val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

  private val _isScanningDeviceApps = MutableStateFlow(false)
  val isScanningDeviceApps: StateFlow<Boolean> = _isScanningDeviceApps.asStateFlow()

  private val _statusBannerMessage = MutableStateFlow<String?>(null)
  val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

  private val _editingInstance = MutableStateFlow<ClonedInstanceEntity?>(null)
  val editingInstance: StateFlow<ClonedInstanceEntity?> = _editingInstance.asStateFlow()

  private val _preselectedPlatformForClone = MutableStateFlow<SocialPlatform?>(null)
  val preselectedPlatformForClone: StateFlow<SocialPlatform?> = _preselectedPlatformForClone.asStateFlow()

  init {
    refreshInstalledDeviceApps()
  }

  fun selectTab(destination: TwinSpaceDestination) {
    _currentTab.value = destination
  }

  fun selectWorkspaceGroup(group: String) {
    _selectedWorkspaceGroup.value = group
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun dismissStatusBanner() {
    _statusBannerMessage.value = null
  }

  fun showStatusMessage(message: String) {
    _statusBannerMessage.value = message
  }

  fun openCloneCreatorModal(platform: SocialPlatform?) {
    _preselectedPlatformForClone.value = platform
  }

  fun openInstanceSettingsModal(instance: ClonedInstanceEntity?) {
    _editingInstance.value = instance
  }

  fun dismissPinModal() {
    _pendingPinInstance.value = null
  }

  fun refreshInstalledDeviceApps() {
    viewModelScope.launch {
      _isScanningDeviceApps.value = true
      val discovered = repository.scanDeviceInstalledApps()
      _installedApps.value = discovered
      _isScanningDeviceApps.value = false
    }
  }

  fun launchInstance(instance: ClonedInstanceEntity, inSecondaryPane: Boolean = false) {
    if (instance.hasPinLock && instance.id !in _unlockedInstanceIds.value) {
      _pendingPinInstance.value = instance
      _pendingPinTargetSecondary.value = inSecondaryPane
      return
    }
    executeLaunch(instance, inSecondaryPane)
  }

  private fun executeLaunch(instance: ClonedInstanceEntity, inSecondaryPane: Boolean) {
    viewModelScope.launch {
      SessionSandboxEngine.applyIsolatedCookiesForInstance(instance)
      repository.recordLaunch(instance.id)
      if (inSecondaryPane) {
        _activeSecondaryInstanceId.value = instance.id
        _isDualSplitMode.value = true
        if (_activePrimaryInstanceId.value == null) {
          val fallbackPrimary = allInstances.value.firstOrNull { it.id != instance.id } ?: instance
          _activePrimaryInstanceId.value = fallbackPrimary.id
        }
      } else {
        _activePrimaryInstanceId.value = instance.id
      }
      _currentTab.value = TwinSpaceDestination.RUNNING_SANDBOX
      _statusBannerMessage.value =
        "Mounted ${instance.platformName} (${instance.badgeLabel}) in isolated sandbox ${instance.sandboxNamespace}"
    }
  }

  fun verifyPinAndLaunch(enteredPin: String): Boolean {
    val target = _pendingPinInstance.value ?: return false
    if (target.pinCode == enteredPin.trim()) {
      _unlockedInstanceIds.value = _unlockedInstanceIds.value + target.id
      val inSecondary = _pendingPinTargetSecondary.value
      _pendingPinInstance.value = null
      executeLaunch(target, inSecondary)
      return true
    }
    return false
  }

  fun launchDualSplitPair(primary: ClonedInstanceEntity, secondary: ClonedInstanceEntity) {
    viewModelScope.launch {
      SessionSandboxEngine.applyIsolatedCookiesForInstance(primary)
      repository.recordLaunch(primary.id)
      repository.recordLaunch(secondary.id)
      _activePrimaryInstanceId.value = primary.id
      _activeSecondaryInstanceId.value = secondary.id
      _isDualSplitMode.value = true
      _currentTab.value = TwinSpaceDestination.RUNNING_SANDBOX
      _statusBannerMessage.value =
        "Parallel Split View active: ${primary.instanceName} (${primary.badgeLabel}) + ${secondary.instanceName} (${secondary.badgeLabel})"
    }
  }

  fun toggleDualSplitMode() {
    val nextState = !_isDualSplitMode.value
    _isDualSplitMode.value = nextState
    if (nextState && _activeSecondaryInstanceId.value == null) {
      val currentPrimaryId = _activePrimaryInstanceId.value
      val currentPrimary = allInstances.value.find { it.id == currentPrimaryId }
      // Prefer picking a second clone of the SAME social media platform if one exists!
      val samePlatformSibling = allInstances.value.firstOrNull {
        it.id != currentPrimaryId && it.platformId == currentPrimary?.platformId && !it.hasPinLock
      }
      val anyOtherInstance = allInstances.value.firstOrNull {
        it.id != currentPrimaryId && !it.hasPinLock
      }
      _activeSecondaryInstanceId.value = (samePlatformSibling ?: anyOtherInstance ?: currentPrimary)?.id
    }
  }

  fun swapSplitScreenPanes() {
    val primary = _activePrimaryInstanceId.value
    val secondary = _activeSecondaryInstanceId.value
    if (primary != null && secondary != null) {
      _activePrimaryInstanceId.value = secondary
      _activeSecondaryInstanceId.value = primary
      _statusBannerMessage.value = "Swapped Primary & Secondary sandbox panes"
    }
  }

  fun quickDuplicateInstance(source: ClonedInstanceEntity) {
    viewModelScope.launch {
      val created = repository.duplicateExistingInstance(source)
      _statusBannerMessage.value =
        "Created isolated clone: ${created.platformName} ${created.badgeLabel} (${created.sandboxNamespace})"
    }
  }

  fun createNewClone(
    platformId: String,
    platformName: String,
    monogram: String,
    instanceName: String,
    badgeLabel: String,
    accountHandle: String,
    workspaceGroup: String,
    baseUrl: String,
    accentColorHex: Long,
    userAgentMode: UserAgentMode,
    privacyTrackerBlock: Boolean,
    spoofDeviceFingerprint: Boolean,
    forceDarkWebMode: Boolean,
    incognitoOnExit: Boolean,
    pinCode: String,
    quickNotes: String,
    launchImmediately: Boolean,
  ) {
    viewModelScope.launch {
      val cleanUrl = if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
        baseUrl.trim()
      } else {
        "https://${baseUrl.trim()}"
      }
      val entity = ClonedInstanceEntity(
        platformId = platformId,
        platformName = platformName.trim().ifBlank { "Custom Social" },
        monogram = monogram.trim().take(2).uppercase().ifBlank { "CS" },
        instanceName = instanceName.trim().ifBlank { "Clone Instance" },
        badgeLabel = badgeLabel.trim().take(4).uppercase().ifBlank { "#1" },
        accountHandle = accountHandle.trim().ifBlank { "@multi_user" },
        workspaceGroup = workspaceGroup,
        baseUrl = cleanUrl,
        lastVisitedUrl = cleanUrl,
        accentColorHex = accentColorHex,
        userAgentMode = userAgentMode.name,
        privacyTrackerBlock = privacyTrackerBlock,
        spoofDeviceFingerprint = spoofDeviceFingerprint,
        forceDarkWebMode = forceDarkWebMode,
        incognitoOnExit = incognitoOnExit,
        pinCode = pinCode.filter { it.isDigit() }.take(4),
        quickNotes = quickNotes.trim(),
      )
      val newId = repository.createClone(entity)
      val saved = entity.copy(id = newId)
      _preselectedPlatformForClone.value = null
      if (saved.hasPinLock) {
        _unlockedInstanceIds.value = _unlockedInstanceIds.value + newId
      }
      if (launchImmediately) {
        executeLaunch(saved, inSecondaryPane = false)
      } else {
        _statusBannerMessage.value =
          "Cloned ${saved.platformName} • ${saved.instanceName} (${saved.badgeLabel})"
      }
    }
  }

  fun updateInstanceDetails(updated: ClonedInstanceEntity) {
    viewModelScope.launch {
      repository.updateInstance(updated)
      _editingInstance.value = null
      _statusBannerMessage.value = "Updated sandbox rules for ${updated.instanceName}"
    }
  }

  fun toggleFavoriteInstance(instance: ClonedInstanceEntity) {
    viewModelScope.launch {
      repository.updateInstance(
        instance.copy(isPinnedFavorite = !instance.isPinnedFavorite)
      )
    }
  }

  fun deleteInstance(instance: ClonedInstanceEntity) {
    viewModelScope.launch {
      if (_activePrimaryInstanceId.value == instance.id) {
        _activePrimaryInstanceId.value = allInstances.value.firstOrNull { it.id != instance.id }?.id
      }
      if (_activeSecondaryInstanceId.value == instance.id) {
        _activeSecondaryInstanceId.value = null
        _isDualSplitMode.value = false
      }
      repository.deleteInstance(instance.id)
      _editingInstance.value = null
      _statusBannerMessage.value =
        "Deleted clone ${instance.platformName} (${instance.badgeLabel}) & purged sandbox data"
    }
  }

  fun wipeInstanceSessionData(instance: ClonedInstanceEntity) {
    viewModelScope.launch {
      repository.wipeInstanceSession(instance.id)
      _statusBannerMessage.value =
        "Wiped cookies & localStorage vault for ${instance.platformName} (${instance.badgeLabel})"
    }
  }

  fun wipeAllEphemeralSessions() {
    viewModelScope.launch {
      val targets = allInstances.value.filter { it.incognitoOnExit }
      targets.forEach { repository.wipeInstanceSession(it.id) }
      _statusBannerMessage.value =
        if (targets.isNotEmpty()) {
          "Purged sessions for ${targets.size} ephemeral clone(s)"
        } else {
          "All active sandboxes verified clean"
        }
    }
  }

  fun persistLiveWebSession(
    instance: ClonedInstanceEntity,
    currentUrl: String,
    localStorageJson: String?,
    sessionStorageJson: String?,
    storageKeyCount: Int?,
    additionalBlockedTrackers: Int = 0,
  ) {
    viewModelScope.launch {
      val (cookieJson, cookieCount) = SessionSandboxEngine.captureCookiesAsJson(
        url = currentUrl,
        fallbackExistingJson = instance.cookieJarJson,
      )
      repository.saveSessionSnapshot(
        id = instance.id,
        url = currentUrl.ifBlank { instance.lastVisitedUrl },
        cookieJarJson = cookieJson,
        localStorageJson = localStorageJson ?: instance.localStorageJson,
        sessionStorageJson = sessionStorageJson ?: instance.sessionStorageJson,
        cookieCount = cookieCount,
        storageKeyCount = storageKeyCount ?: instance.storageKeyCount,
        blockedTrackersCount = instance.blockedTrackersCount + additionalBlockedTrackers,
      )
    }
  }

  fun pinToHomeScreen(instance: ClonedInstanceEntity) {
    val requested = repository.pinInstanceShortcutToLauncher(instance)
    _statusBannerMessage.value = if (requested) {
      "Sent Home Screen shortcut request for ${instance.platformName} (${instance.badgeLabel})"
    } else {
      "Shortcut ready: ${instance.platformName} (${instance.badgeLabel}) pinned in Quick Favorites"
    }
    if (!instance.isPinnedFavorite) {
      toggleFavoriteInstance(instance)
    }
  }

  fun handleDeepLinkInstanceId(instanceId: Long) {
    if (instanceId <= 0L) return
    viewModelScope.launch {
      val found = repository.getInstanceById(instanceId)
      if (found != null) {
        launchInstance(found, inSecondaryPane = false)
      }
    }
  }

  companion object {
    fun provideFactory(application: Application): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val db = TwinSpaceDatabase.getInstance(application)
          val repo = CloneRepository(db.clonedInstanceDao(), application)
          return TwinSpaceViewModel(application, repo) as T
        }
      }
  }
}
