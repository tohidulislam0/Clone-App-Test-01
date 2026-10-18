package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SocialPlatformCatalog
import com.example.ui.components.CreateOrEditCloneDialog
import com.example.ui.components.PinUnlockDialog
import com.example.ui.screens.CloneCatalogScreen
import com.example.ui.screens.RunningSandboxScreen
import com.example.ui.screens.SandboxAuditScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismViolet

@Composable
fun TwinSpaceApp(
  viewModel: TwinSpaceViewModel,
  modifier: Modifier = Modifier,
) {
  val instances by viewModel.allInstances.collectAsStateWithLifecycle()
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val selectedGroup by viewModel.selectedWorkspaceGroup.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val activePrimaryId by viewModel.activePrimaryInstanceId.collectAsStateWithLifecycle()
  val activeSecondaryId by viewModel.activeSecondaryInstanceId.collectAsStateWithLifecycle()
  val isDualSplitMode by viewModel.isDualSplitMode.collectAsStateWithLifecycle()
  val pendingPinInstance by viewModel.pendingPinInstance.collectAsStateWithLifecycle()
  val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
  val isScanningApps by viewModel.isScanningDeviceApps.collectAsStateWithLifecycle()
  val statusMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
  val editingInstance by viewModel.editingInstance.collectAsStateWithLifecycle()
  val preselectedPlatform by viewModel.preselectedPlatformForClone.collectAsStateWithLifecycle()

  var showCreateDialog by remember { mutableStateOf(false) }

  // BackHandler for secondary tabs to return to Workspace
  if (currentTab != TwinSpaceDestination.WORKSPACE) {
    BackHandler {
      viewModel.selectTab(TwinSpaceDestination.WORKSPACE)
    }
  }

  val primaryInstance = remember(instances, activePrimaryId) {
    instances.find { it.id == activePrimaryId } ?: instances.firstOrNull()
  }
  val secondaryInstance = remember(instances, activeSecondaryId, primaryInstance) {
    instances.find { it.id == activeSecondaryId }
      ?: instances.firstOrNull { it.id != primaryInstance?.id }
      ?: primaryInstance
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize().background(ObsidianBg)) {
    val isExpandedScreen = maxWidth >= 680.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = ObsidianBg,
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        TwinSpaceTopBar(
          cloneCount = instances.size,
          isDualSplitMode = isDualSplitMode,
          onQuickDualLaunch = {
            if (currentTab != TwinSpaceDestination.RUNNING_SANDBOX) {
              val first = primaryInstance ?: instances.getOrNull(0)
              val second = secondaryInstance ?: instances.getOrNull(1)
              if (first != null && second != null) {
                viewModel.launchDualSplitPair(first, second)
              }
            } else {
              viewModel.toggleDualSplitMode()
            }
          },
          onNewCloneClick = {
            viewModel.openCloneCreatorModal(null)
            showCreateDialog = true
          },
        )
      },
      bottomBar = {
        if (!isExpandedScreen) {
          TwinSpaceBottomNavigationBar(
            currentTab = currentTab,
            runningCloneBadge = if (isDualSplitMode) "2x" else "1x",
            onSelectTab = viewModel::selectTab,
          )
        }
      },
      floatingActionButton = {
        if (currentTab == TwinSpaceDestination.WORKSPACE && !isExpandedScreen) {
          FloatingActionButton(
            onClick = {
              viewModel.openCloneCreatorModal(null)
              showCreateDialog = true
            },
            containerColor = ElectricCyan,
            contentColor = ObsidianBg,
            modifier = Modifier.testTag("fab_create_clone"),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Clone new social media instance",
            )
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        if (isExpandedScreen) {
          TwinSpaceSideNavigationRail(
            currentTab = currentTab,
            runningCloneBadge = if (isDualSplitMode) "2x" else "1x",
            onSelectTab = viewModel::selectTab,
            onCreateClone = {
              viewModel.openCloneCreatorModal(null)
              showCreateDialog = true
            },
          )
        }

        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
          // Animated Status Banner Toast
          AnimatedVisibility(
            visible = statusMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
          ) {
            statusMessage?.let { msg ->
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 6.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, NeonEmerald.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                color = ObsidianSurfaceElevated,
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonEmerald,
                    modifier = Modifier.weight(1f),
                  )
                  IconButton(
                    onClick = viewModel::dismissStatusBanner,
                    modifier = Modifier.size(28.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Close,
                      contentDescription = "Dismiss notification",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
              }
            }
          }

          when (currentTab) {
            TwinSpaceDestination.WORKSPACE -> {
              WorkspaceScreen(
                instances = instances,
                selectedGroup = selectedGroup,
                searchQuery = searchQuery,
                onSelectGroup = viewModel::selectWorkspaceGroup,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onLaunchInstance = { viewModel.launchInstance(it, inSecondaryPane = false) },
                onLaunchInSecondarySplit = { viewModel.launchInstance(it, inSecondaryPane = true) },
                onLaunchDualPair = viewModel::launchDualSplitPair,
                onDuplicateInstance = viewModel::quickDuplicateInstance,
                onOpenCreateClone = { platformId ->
                  val platform = platformId?.let { SocialPlatformCatalog.findById(it) }
                  viewModel.openCloneCreatorModal(platform)
                  showCreateDialog = true
                },
                onOpenInstanceSettings = { viewModel.openInstanceSettingsModal(it) },
                onToggleFavorite = viewModel::toggleFavoriteInstance,
                onPinShortcut = viewModel::pinToHomeScreen,
              )
            }

            TwinSpaceDestination.RUNNING_SANDBOX -> {
              RunningSandboxScreen(
                allInstances = instances,
                primaryInstance = primaryInstance,
                secondaryInstance = secondaryInstance,
                isDualSplitMode = isDualSplitMode,
                onSelectPrimaryInstance = { viewModel.launchInstance(it, inSecondaryPane = false) },
                onSelectSecondaryInstance = { viewModel.launchInstance(it, inSecondaryPane = true) },
                onToggleDualSplitMode = viewModel::toggleDualSplitMode,
                onSwapPanes = viewModel::swapSplitScreenPanes,
                onPersistWebSession = viewModel::persistLiveWebSession,
                onWipeSession = viewModel::wipeInstanceSessionData,
                onBackToWorkspace = { viewModel.selectTab(TwinSpaceDestination.WORKSPACE) },
                onShowToast = viewModel::showStatusMessage,
              )
            }

            TwinSpaceDestination.CLONE_CATALOG -> {
              CloneCatalogScreen(
                existingInstances = instances,
                installedApps = installedApps,
                isScanningDeviceApps = isScanningApps,
                onRefreshDeviceApps = viewModel::refreshInstalledDeviceApps,
                onSelectPlatformToClone = { platform ->
                  viewModel.openCloneCreatorModal(platform)
                  showCreateDialog = true
                },
                onSelectInstalledAppToClone = { appInfo ->
                  val matched = appInfo.matchedPlatform
                  if (matched != null) {
                    viewModel.openCloneCreatorModal(matched)
                    showCreateDialog = true
                  } else {
                    val count = instances.size + 1
                    viewModel.createNewClone(
                      platformId = "pkg_${appInfo.packageName.replace('.', '_')}",
                      platformName = appInfo.appLabel,
                      monogram = appInfo.appLabel.take(2).uppercase(),
                      instanceName = "${appInfo.appLabel} Clone",
                      badgeLabel = "#$count",
                      accountHandle = "@device_clone",
                      workspaceGroup = "Personal",
                      baseUrl = appInfo.fallbackWebUrl,
                      accentColorHex = 0xFF10B981L,
                      userAgentMode = com.example.data.model.UserAgentMode.ANDROID_MOBILE,
                      privacyTrackerBlock = true,
                      spoofDeviceFingerprint = true,
                      forceDarkWebMode = false,
                      incognitoOnExit = false,
                      pinCode = "",
                      quickNotes = "Cloned from device package: ${appInfo.packageName}",
                      launchImmediately = true,
                    )
                  }
                },
                onSelectCustomWebAppToClone = {
                  viewModel.openCloneCreatorModal(null)
                  showCreateDialog = true
                },
              )
            }

            TwinSpaceDestination.SECURITY_AUDIT -> {
              SandboxAuditScreen(
                instances = instances,
                onWipeInstanceSession = viewModel::wipeInstanceSessionData,
                onWipeAllEphemeral = viewModel::wipeAllEphemeralSessions,
                onOpenInstanceSettings = { viewModel.openInstanceSettingsModal(it) },
                onDeleteInstance = viewModel::deleteInstance,
              )
            }
          }
        }
      }
    }
  }

  // Create or Edit Clone Dialog
  if (showCreateDialog || editingInstance != null) {
    val activePlatform = preselectedPlatform
      ?: editingInstance?.let { SocialPlatformCatalog.findById(it.platformId) }
      ?: SocialPlatformCatalog.platforms.first()
    val countForPlatform = instances.count { it.platformId == activePlatform.id }

    CreateOrEditCloneDialog(
      initialPlatform = preselectedPlatform,
      editingInstance = editingInstance,
      existingCloneCountForPlatform = countForPlatform,
      onDismiss = {
        showCreateDialog = false
        viewModel.openCloneCreatorModal(null)
        viewModel.openInstanceSettingsModal(null)
      },
      onCreateClone = {
        platformId,
        platformName,
        monogram,
        instanceName,
        badgeLabel,
        accountHandle,
        workspaceGroup,
        baseUrl,
        accentColorHex,
        userAgentMode,
        privacyTrackerBlock,
        spoofDeviceFingerprint,
        forceDarkWebMode,
        incognitoOnExit,
        pinCode,
        quickNotes,
        launchImmediately,
        ->
        showCreateDialog = false
        viewModel.createNewClone(
          platformId = platformId,
          platformName = platformName,
          monogram = monogram,
          instanceName = instanceName,
          badgeLabel = badgeLabel,
          accountHandle = accountHandle,
          workspaceGroup = workspaceGroup,
          baseUrl = baseUrl,
          accentColorHex = accentColorHex,
          userAgentMode = userAgentMode,
          privacyTrackerBlock = privacyTrackerBlock,
          spoofDeviceFingerprint = spoofDeviceFingerprint,
          forceDarkWebMode = forceDarkWebMode,
          incognitoOnExit = incognitoOnExit,
          pinCode = pinCode,
          quickNotes = quickNotes,
          launchImmediately = launchImmediately,
        )
      },
      onUpdateClone = { updated ->
        showCreateDialog = false
        viewModel.updateInstanceDetails(updated)
      },
      onDeleteClone = { toDelete ->
        showCreateDialog = false
        viewModel.deleteInstance(toDelete)
      },
    )
  }

  // 4-Digit PIN Verification Dialog
  pendingPinInstance?.let { lockedInstance ->
    PinUnlockDialog(
      instance = lockedInstance,
      onDismiss = viewModel::dismissPinModal,
      onVerifyPin = viewModel::verifyPinAndLaunch,
    )
  }
}

@Composable
private fun TwinSpaceTopBar(
  cloneCount: Int,
  isDualSplitMode: Boolean,
  onQuickDualLaunch: () -> Unit,
  onNewCloneClick: () -> Unit,
) {
  Surface(
    color = ObsidianSurface,
    tonalElevation = 3.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // Brand Emblem
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(11.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(ElectricCyan, PrismViolet)
                )
              ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Layers,
              contentDescription = "TwinSpace logo",
              tint = ObsidianBg,
              modifier = Modifier.size(22.dp),
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = "TwinSpace",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Surface(
                color = ElectricCyan.copy(alpha = 0.16f),
                shape = RoundedCornerShape(6.dp),
              ) {
                Text(
                  text = "$cloneCount CLONES",
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  color = ElectricCyan,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
            Text(
              text = "Multi-Instance Social Container",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onQuickDualLaunch)
              .testTag("topbar_dual_split_button"),
            color = if (isDualSplitMode) PrismViolet else ObsidianSurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isDualSplitMode) Color.White.copy(alpha = 0.5f) else PrismViolet.copy(alpha = 0.5f),
            ),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.VerticalSplit,
                contentDescription = "Dual split screen",
                tint = if (isDualSplitMode) Color.White else PrismViolet,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = if (isDualSplitMode) "Dual Active" else "Dual View",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
              )
            }
          }

          IconButton(
            onClick = onNewCloneClick,
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(ElectricCyan.copy(alpha = 0.18f))
              .testTag("topbar_add_clone_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Add new clone",
              tint = ElectricCyan,
            )
          }
        }
      }
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }
  }
}

private data class NavDestinationMeta(
  val destination: TwinSpaceDestination,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val navItems = listOf(
  NavDestinationMeta(
    destination = TwinSpaceDestination.WORKSPACE,
    selectedIcon = Icons.Filled.GridView,
    unselectedIcon = Icons.Outlined.GridView,
  ),
  NavDestinationMeta(
    destination = TwinSpaceDestination.RUNNING_SANDBOX,
    selectedIcon = Icons.Filled.Layers,
    unselectedIcon = Icons.Outlined.Layers,
  ),
  NavDestinationMeta(
    destination = TwinSpaceDestination.CLONE_CATALOG,
    selectedIcon = Icons.Filled.Apps,
    unselectedIcon = Icons.Outlined.Apps,
  ),
  NavDestinationMeta(
    destination = TwinSpaceDestination.SECURITY_AUDIT,
    selectedIcon = Icons.Filled.Security,
    unselectedIcon = Icons.Outlined.Security,
  ),
)

@Composable
private fun TwinSpaceBottomNavigationBar(
  currentTab: TwinSpaceDestination,
  runningCloneBadge: String,
  onSelectTab: (TwinSpaceDestination) -> Unit,
) {
  NavigationBar(
    containerColor = ObsidianSurface,
    tonalElevation = 8.dp,
  ) {
    navItems.forEach { item ->
      val selected = currentTab == item.destination
      NavigationBarItem(
        selected = selected,
        onClick = { onSelectTab(item.destination) },
        modifier = Modifier.testTag("nav_tab_${item.destination.route}"),
        icon = {
          if (item.destination == TwinSpaceDestination.RUNNING_SANDBOX) {
            BadgedBox(
              badge = {
                Badge(
                  containerColor = PrismViolet,
                  contentColor = Color.White,
                ) {
                  Text(runningCloneBadge)
                }
              }
            ) {
              Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.destination.label,
              )
            }
          } else {
            Icon(
              imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
              contentDescription = item.destination.label,
            )
          }
        },
        label = {
          Text(
            text = item.destination.label,
            style = MaterialTheme.typography.labelMedium,
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = ElectricCyan,
          selectedTextColor = ElectricCyan,
          indicatorColor = ElectricCyan.copy(alpha = 0.16f),
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
  }
}

@Composable
private fun TwinSpaceSideNavigationRail(
  currentTab: TwinSpaceDestination,
  runningCloneBadge: String,
  onSelectTab: (TwinSpaceDestination) -> Unit,
  onCreateClone: () -> Unit,
) {
  NavigationRail(
    containerColor = ObsidianSurface,
    header = {
      FloatingActionButton(
        onClick = onCreateClone,
        containerColor = ElectricCyan,
        contentColor = ObsidianBg,
        modifier = Modifier
          .padding(top = 12.dp)
          .testTag("rail_create_clone_button"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Create new clone")
      }
    },
  ) {
    Spacer(modifier = Modifier.weight(1f))
    navItems.forEach { item ->
      val selected = currentTab == item.destination
      NavigationRailItem(
        selected = selected,
        onClick = { onSelectTab(item.destination) },
        modifier = Modifier.testTag("rail_tab_${item.destination.route}"),
        icon = {
          if (item.destination == TwinSpaceDestination.RUNNING_SANDBOX) {
            BadgedBox(
              badge = {
                Badge(containerColor = PrismViolet, contentColor = Color.White) {
                  Text(runningCloneBadge)
                }
              }
            ) {
              Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.destination.label,
              )
            }
          } else {
            Icon(
              imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
              contentDescription = item.destination.label,
            )
          }
        },
        label = { Text(item.destination.label) },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = ElectricCyan,
          selectedTextColor = ElectricCyan,
          indicatorColor = ElectricCyan.copy(alpha = 0.16f),
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
    Spacer(modifier = Modifier.weight(1f))
  }
}
