package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.SocialPlatformCatalog
import com.example.data.model.UserAgentMode
import com.example.ui.components.InstanceAvatarBadge
import com.example.ui.components.TwinSpaceHeroBanner
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismViolet

@Composable
fun WorkspaceScreen(
  instances: List<ClonedInstanceEntity>,
  selectedGroup: String,
  searchQuery: String,
  onSelectGroup: (String) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onLaunchInstance: (ClonedInstanceEntity) -> Unit,
  onLaunchInSecondarySplit: (ClonedInstanceEntity) -> Unit,
  onLaunchDualPair: (ClonedInstanceEntity, ClonedInstanceEntity) -> Unit,
  onDuplicateInstance: (ClonedInstanceEntity) -> Unit,
  onOpenCreateClone: (String?) -> Unit,
  onOpenInstanceSettings: (ClonedInstanceEntity) -> Unit,
  onToggleFavorite: (ClonedInstanceEntity) -> Unit,
  onPinShortcut: (ClonedInstanceEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  val filteredInstances = remember(instances, selectedGroup, searchQuery) {
    instances.filter { item ->
      val matchesGroup = selectedGroup == "All" || item.workspaceGroup.equals(selectedGroup, ignoreCase = true)
      val matchesSearch = searchQuery.isBlank() ||
        item.instanceName.contains(searchQuery, ignoreCase = true) ||
        item.platformName.contains(searchQuery, ignoreCase = true) ||
        item.accountHandle.contains(searchQuery, ignoreCase = true) ||
        item.badgeLabel.contains(searchQuery, ignoreCase = true)
      matchesGroup && matchesSearch
    }
  }

  val groupedByPlatform = remember(filteredInstances) {
    filteredInstances.groupBy { it.platformId }
  }

  val multiInstanceCount = remember(instances) {
    instances.groupBy { it.platformId }.count { it.value.size >= 2 }
  }

  val totalBlockedTrackers = remember(instances) {
    instances.sumOf { it.blockedTrackersCount }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("workspace_lazy_column"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item(key = "hero_banner") {
      TwinSpaceHeroBanner(
        totalClones = instances.size,
        multiInstancePlatforms = multiInstanceCount,
        totalTrackersBlocked = totalBlockedTrackers,
        onCreateCloneClick = { onOpenCreateClone(null) },
        onLaunchDualViewClick = {
          val multiGroup = instances.groupBy { it.platformId }.values.firstOrNull { it.size >= 2 }
          val first = multiGroup?.getOrNull(0) ?: instances.getOrNull(0)
          val second = multiGroup?.getOrNull(1) ?: instances.getOrNull(1)
          if (first != null && second != null) {
            onLaunchDualPair(first, second)
          } else if (first != null) {
            onLaunchInstance(first)
          } else {
            onOpenCreateClone(null)
          }
        },
      )
    }

    item(key = "search_and_filters") {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("workspace_search_input"),
          placeholder = {
            Text(
              text = "Search cloned apps, @handles, or #badges...",
              style = MaterialTheme.typography.bodyMedium,
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search clones",
              tint = ElectricCyan,
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { onSearchQueryChange("") },
                modifier = Modifier.testTag("clear_search_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Clear,
                  contentDescription = "Clear search query",
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricCyan,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = ObsidianSurface,
            unfocusedContainerColor = ObsidianSurface,
          ),
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          items(SocialPlatformCatalog.workspaceGroups, key = { it }) { group ->
            val selected = group == selectedGroup
            FilterChip(
              selected = selected,
              onClick = { onSelectGroup(group) },
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("filter_chip_${group.lowercase().replace(" ", "_")}"),
              label = {
                Text(
                  text = group,
                  style = MaterialTheme.typography.labelLarge,
                )
              },
              leadingIcon = if (selected) {
                {
                  Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                }
              } else null,
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                selectedLabelColor = ElectricCyan,
                selectedLeadingIconColor = ElectricCyan,
                containerColor = ObsidianSurface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = ElectricCyan,
              ),
            )
          }
        }
      }
    }

    if (filteredInstances.isEmpty()) {
      item(key = "empty_workspace_state") {
        EmptyWorkspaceCard(
          onResetFilter = {
            onSelectGroup("All")
            onSearchQueryChange("")
          },
          onCreateClone = { onOpenCreateClone(null) },
        )
      }
    } else {
      groupedByPlatform.entries.forEach { (platformId, platformClones) ->
        val firstClone = platformClones.first()
        item(key = "platform_group_$platformId") {
          PlatformMultiInstanceGroupCard(
            platformId = platformId,
            platformName = firstClone.platformName,
            clones = platformClones,
            onLaunchInstance = onLaunchInstance,
            onLaunchInSecondarySplit = onLaunchInSecondarySplit,
            onLaunchDualPair = onLaunchDualPair,
            onDuplicateInstance = onDuplicateInstance,
            onAddAnotherForPlatform = { onOpenCreateClone(platformId) },
            onOpenInstanceSettings = onOpenInstanceSettings,
            onToggleFavorite = onToggleFavorite,
            onPinShortcut = onPinShortcut,
          )
        }
      }
    }

    item(key = "bottom_spacer") {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun PlatformMultiInstanceGroupCard(
  platformId: String,
  platformName: String,
  clones: List<ClonedInstanceEntity>,
  onLaunchInstance: (ClonedInstanceEntity) -> Unit,
  onLaunchInSecondarySplit: (ClonedInstanceEntity) -> Unit,
  onLaunchDualPair: (ClonedInstanceEntity, ClonedInstanceEntity) -> Unit,
  onDuplicateInstance: (ClonedInstanceEntity) -> Unit,
  onAddAnotherForPlatform: () -> Unit,
  onOpenInstanceSettings: (ClonedInstanceEntity) -> Unit,
  onToggleFavorite: (ClonedInstanceEntity) -> Unit,
  onPinShortcut: (ClonedInstanceEntity) -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("platform_group_card_$platformId"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f),
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Platform Header Row with Clone Count & Quick Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          Text(
            text = platformName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            color = if (clones.size >= 2) ElectricCyan.copy(alpha = 0.16f) else PrismViolet.copy(alpha = 0.16f),
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = "${clones.size} ${if (clones.size == 1) "INSTANCE" else "PARALLEL INSTANCES"}",
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall,
              color = if (clones.size >= 2) ElectricCyan else PrismViolet,
              fontWeight = FontWeight.Bold,
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (clones.size >= 2) {
            OutlinedButton(
              onClick = { onLaunchDualPair(clones[0], clones[1]) },
              modifier = Modifier
                .height(40.dp)
                .testTag("run_dual_pair_$platformId"),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, PrismViolet.copy(alpha = 0.7f)),
            ) {
              Icon(
                imageVector = Icons.Filled.VerticalSplit,
                contentDescription = "Run two instances side by side",
                tint = PrismViolet,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Dual Run",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
              )
            }
          }

          OutlinedButton(
            onClick = { onDuplicateInstance(clones.last()) },
            modifier = Modifier
              .height(40.dp)
              .testTag("quick_clone_plus_one_$platformId"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Clone another instance",
              tint = ElectricCyan,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+1 Clone",
              style = MaterialTheme.typography.labelMedium,
              color = ElectricCyan,
            )
          }
        }
      }

      // Individual Cloned Instances inside this platform group
      clones.forEach { instance ->
        ClonedInstanceItemCard(
          instance = instance,
          onLaunch = { onLaunchInstance(instance) },
          onLaunchSplit = { onLaunchInSecondarySplit(instance) },
          onDuplicate = { onDuplicateInstance(instance) },
          onOpenSettings = { onOpenInstanceSettings(instance) },
          onToggleFavorite = { onToggleFavorite(instance) },
          onPinShortcut = { onPinShortcut(instance) },
        )
      }
    }
  }
}

@Composable
private fun ClonedInstanceItemCard(
  instance: ClonedInstanceEntity,
  onLaunch: () -> Unit,
  onLaunchSplit: () -> Unit,
  onDuplicate: () -> Unit,
  onOpenSettings: () -> Unit,
  onToggleFavorite: () -> Unit,
  onPinShortcut: () -> Unit,
) {
  val accentColor = Color(instance.accentColorHex or 0xFF000000L)
  val uaMode = UserAgentMode.fromName(instance.userAgentMode)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(ObsidianSurfaceElevated)
      .border(
        width = 1.dp,
        color = accentColor.copy(alpha = 0.32f),
        shape = RoundedCornerShape(18.dp),
      )
      .clickable(onClick = onLaunch)
      .padding(14.dp)
      .testTag("instance_card_${instance.id}"),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      InstanceAvatarBadge(
        monogram = instance.monogram,
        badgeLabel = instance.badgeLabel,
        accentColorHex = instance.accentColorHex,
        hasPinLock = instance.hasPinLock,
      )

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = instance.instanceName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Surface(
            color = accentColor.copy(alpha = 0.16f),
            shape = RoundedCornerShape(6.dp),
          ) {
            Text(
              text = instance.workspaceGroup,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall,
              color = accentColor,
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "${instance.accountHandle} • ${instance.sandboxNamespace}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Sandbox Isolation Telemetry Pills
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TelemetryBadgePill(
            text = "UA: ${uaMode.shortTag}",
            tint = ElectricCyan,
          )
          TelemetryBadgePill(
            text = "${instance.cookieCount} Cookies",
            tint = NeonEmerald,
          )
          TelemetryBadgePill(
            text = instance.deviceFingerprintHash,
            tint = PrismViolet,
          )
        }
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .testTag("favorite_button_${instance.id}"),
      ) {
        Icon(
          imageVector = if (instance.isPinnedFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
          contentDescription = "Favorite instance",
          tint = if (instance.isPinnedFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    if (instance.quickNotes.isNotBlank()) {
      Text(
        text = instance.quickNotes,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    // Action Bar inside Instance Card
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Button(
        onClick = onLaunch,
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("launch_instance_button_${instance.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = accentColor,
          contentColor = ObsidianBg,
        ),
      ) {
        Icon(
          imageVector = Icons.Filled.PlayArrow,
          contentDescription = "Launch isolated instance",
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (instance.hasPinLock) "Unlock & Run" else "Run Instance",
          style = MaterialTheme.typography.labelLarge,
        )
      }

      IconButton(
        onClick = onLaunchSplit,
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(ObsidianBg)
          .testTag("split_instance_button_${instance.id}"),
      ) {
        Icon(
          imageVector = Icons.Filled.VerticalSplit,
          contentDescription = "Open in Dual Split Secondary Pane",
          tint = PrismViolet,
        )
      }

      IconButton(
        onClick = onDuplicate,
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(ObsidianBg)
          .testTag("duplicate_instance_button_${instance.id}"),
      ) {
        Icon(
          imageVector = Icons.Filled.ContentCopy,
          contentDescription = "Duplicate this clone setup",
          tint = ElectricCyan,
        )
      }

      IconButton(
        onClick = onPinShortcut,
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(ObsidianBg)
          .testTag("pin_shortcut_button_${instance.id}"),
      ) {
        Icon(
          imageVector = Icons.Filled.PushPin,
          contentDescription = "Pin clone shortcut to Home Screen",
          tint = NeonEmerald,
        )
      }

      IconButton(
        onClick = onOpenSettings,
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(ObsidianBg)
          .testTag("settings_instance_button_${instance.id}"),
      ) {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = "Configure sandbox & account vault",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun TelemetryBadgePill(
  text: String,
  tint: Color,
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(ObsidianBg)
      .border(0.8.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = tint,
    )
  }
}

@Composable
private fun EmptyWorkspaceCard(
  onResetFilter: () -> Unit,
  onCreateClone: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(ElectricCyan.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Shield,
          contentDescription = "Empty workspace icon",
          tint = ElectricCyan,
          modifier = Modifier.size(32.dp),
        )
      }
      Text(
        text = stringResource(R.string.empty_workspace_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = stringResource(R.string.empty_workspace_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
          onClick = onResetFilter,
          modifier = Modifier
            .height(48.dp)
            .testTag("empty_reset_filter_button"),
        ) {
          Text("Show All Clones")
        }
        Button(
          onClick = onCreateClone,
          modifier = Modifier
            .height(48.dp)
            .testTag("empty_create_clone_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = ElectricCyan,
            contentColor = ObsidianBg,
          ),
        ) {
          Text("Create New Clone")
        }
      }
    }
  }
}
