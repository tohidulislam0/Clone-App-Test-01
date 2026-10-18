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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.InstalledAppInfo
import com.example.data.model.SocialPlatform
import com.example.data.model.SocialPlatformCatalog
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismViolet

@Composable
fun CloneCatalogScreen(
  existingInstances: List<ClonedInstanceEntity>,
  installedApps: List<InstalledAppInfo>,
  isScanningDeviceApps: Boolean,
  onRefreshDeviceApps: () -> Unit,
  onSelectPlatformToClone: (SocialPlatform) -> Unit,
  onSelectInstalledAppToClone: (InstalledAppInfo) -> Unit,
  onSelectCustomWebAppToClone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val instanceCountByPlatform = remember(existingInstances) {
    existingInstances.groupingBy { it.platformId }.eachCount()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("clone_catalog_lazy_column"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item(key = "catalog_header") {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Clone Social Media App",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Spin up unlimited isolated instances of any social platform, device app, or custom web endpoint.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Custom URL / Any Web Social App Cloner Banner
    item(key = "custom_url_cloner") {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onSelectCustomWebAppToClone)
          .testTag("clone_custom_url_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrismViolet.copy(alpha = 0.6f)),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(PrismViolet.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Language,
              contentDescription = "Custom social web app",
              tint = PrismViolet,
              modifier = Modifier.size(26.dp),
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Custom Social Network / Web URL",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Clone any forum, Mastodon server, Slack workspace, or web portal into an isolated container.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Button(
            onClick = onSelectCustomWebAppToClone,
            modifier = Modifier
              .height(48.dp)
              .testTag("clone_custom_url_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = PrismViolet,
              contentColor = Color.White,
            ),
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Custom")
          }
        }
      }
    }

    item(key = "social_catalog_section_title") {
      Text(
        text = "SUPPORTED SOCIAL PLATFORMS (${SocialPlatformCatalog.platforms.size})",
        style = MaterialTheme.typography.labelLarge,
        color = ElectricCyan,
        modifier = Modifier.padding(top = 6.dp),
      )
    }

    items(SocialPlatformCatalog.platforms, key = { it.id }) { platform ->
      val activeCloneCount = instanceCountByPlatform[platform.id] ?: 0
      val brandColor = Color(platform.brandColorHex or 0xFF000000L)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectPlatformToClone(platform) }
          .testTag("catalog_platform_card_${platform.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = androidx.compose.foundation.BorderStroke(
          width = 1.dp,
          color = if (activeCloneCount > 0) brandColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline,
        ),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(15.dp))
              .background(brandColor.copy(alpha = 0.18f))
              .border(1.5.dp, brandColor, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = platform.monogram,
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = platform.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Surface(
                color = ObsidianSurfaceElevated,
                shape = RoundedCornerShape(6.dp),
              ) {
                Text(
                  text = platform.category,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
              text = platform.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )

            if (activeCloneCount > 0) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "$activeCloneCount active clone(s) configured • Tap to create Clone #${activeCloneCount + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
              )
            }
          }

          Button(
            onClick = { onSelectPlatformToClone(platform) },
            modifier = Modifier
              .height(48.dp)
              .testTag("clone_platform_button_${platform.id}"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricCyan,
              contentColor = ObsidianBg,
            ),
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (activeCloneCount > 0) "Clone #${activeCloneCount + 1}" else "Clone",
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }

    // Installed Device Apps Section discovered via PackageManager
    item(key = "installed_device_apps_header") {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.PhoneAndroid,
            contentDescription = "Installed device apps",
            tint = NeonEmerald,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "DISCOVERED ON DEVICE (${installedApps.size})",
            style = MaterialTheme.typography.labelLarge,
            color = NeonEmerald,
          )
        }
        IconButton(
          onClick = onRefreshDeviceApps,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("refresh_device_apps_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = "Rescan installed device apps",
            tint = NeonEmerald,
          )
        }
      }
    }

    if (installedApps.isEmpty()) {
      item(key = "no_device_apps_note") {
        Text(
          text = if (isScanningDeviceApps) "Scanning device packages..." else "Use the Supported Social Platforms above or Custom URL to create isolated clones.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      items(installedApps.take(15), key = { it.packageName }) { appInfo ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectInstalledAppToClone(appInfo) }
            .testTag("installed_app_card_${appInfo.packageName}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NeonEmerald.copy(alpha = 0.16f))
                .border(1.dp, NeonEmerald.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = appInfo.appLabel.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = NeonEmerald,
                fontWeight = FontWeight.Bold,
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = appInfo.appLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = appInfo.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Button(
              onClick = { onSelectInstalledAppToClone(appInfo) },
              modifier = Modifier.height(48.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = NeonEmerald.copy(alpha = 0.2f),
                contentColor = NeonEmerald,
              ),
            ) {
              Text("Sandbox Clone", style = MaterialTheme.typography.labelLarge)
            }
          }
        }
      }
    }

    item(key = "catalog_bottom_spacer") {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
