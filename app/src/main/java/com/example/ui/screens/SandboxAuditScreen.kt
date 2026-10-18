package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.UserAgentMode
import com.example.ui.components.InstanceAvatarBadge
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismViolet

@Composable
fun SandboxAuditScreen(
  instances: List<ClonedInstanceEntity>,
  onWipeInstanceSession: (ClonedInstanceEntity) -> Unit,
  onWipeAllEphemeral: () -> Unit,
  onOpenInstanceSettings: (ClonedInstanceEntity) -> Unit,
  onDeleteInstance: (ClonedInstanceEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  val totalCookies = instances.sumOf { it.cookieCount }
  val totalStorageKeys = instances.sumOf { it.storageKeyCount }
  val totalBlocked = instances.sumOf { it.blockedTrackersCount }
  val pinProtectedCount = instances.count { it.hasPinLock }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("sandbox_audit_lazy_column"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item(key = "audit_header") {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Sandbox Isolation & Telemetry",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Inspect per-instance CookieJar partitions, virtualized DOM storage keys, and anti-fingerprinting shields.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    item(key = "audit_summary_card") {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            AuditStatBox(
              title = "Isolated Cookies",
              value = totalCookies.toString(),
              tint = ElectricCyan,
              modifier = Modifier.weight(1f),
            )
            AuditStatBox(
              title = "DOM Vault Keys",
              value = totalStorageKeys.toString(),
              tint = PrismViolet,
              modifier = Modifier.weight(1f),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            AuditStatBox(
              title = "Trackers Blocked",
              value = totalBlocked.toString(),
              tint = NeonEmerald,
              modifier = Modifier.weight(1f),
            )
            AuditStatBox(
              title = "PIN Locked Clones",
              value = pinProtectedCount.toString(),
              tint = Color(0xFFF59E0B),
              modifier = Modifier.weight(1f),
            )
          }

          Button(
            onClick = onWipeAllEphemeral,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("purge_ephemeral_sessions_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricCyan,
              contentColor = ObsidianBg,
            ),
          ) {
            Icon(Icons.Filled.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verify & Purge Ephemeral Sandboxes", style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }

    items(instances, key = { it.id }) { instance ->
      val accent = Color(instance.accentColorHex or 0xFF000000L)
      val ua = UserAgentMode.fromName(instance.userAgentMode)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("audit_instance_card_${instance.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
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
              size = 46.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${instance.platformName} • ${instance.instanceName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Namespace: ${instance.sandboxNamespace} • Launches: ${instance.launchCount}",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan,
              )
            }
          }

          // Technical Isolation Inspector Block
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(ObsidianBg)
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
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
                text = "Device Hash: ${instance.deviceFingerprintHash} | UA: ${ua.label}",
                style = MaterialTheme.typography.labelSmall,
                color = PrismViolet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Storage,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = "CookieJar (${instance.cookieCount}): ${instance.cookieJarJson.take(64)}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonEmerald,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = "Tracker Shield: ${if (instance.privacyTrackerBlock) "ACTIVE" else "OFF"} (${instance.blockedTrackersCount} blocked) • PIN: ${if (instance.hasPinLock) "LOCKED" else "NONE"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = { onWipeInstanceSession(instance) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("audit_wipe_button_${instance.id}"),
              shape = RoundedCornerShape(12.dp),
            ) {
              Text("Wipe Cookies", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
              onClick = { onOpenInstanceSettings(instance) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("audit_edit_button_${instance.id}"),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Configure", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
              onClick = { onDeleteInstance(instance) },
              modifier = Modifier
                .height(48.dp)
                .testTag("audit_delete_button_${instance.id}"),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.6f)),
            ) {
              Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = "Delete clone instance",
                tint = CrimsonAlert,
              )
            }
          }
        }
      }
    }

    item(key = "audit_bottom_spacer") {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun AuditStatBox(
  title: String,
  value: String,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = ObsidianBg,
    border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.3f)),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        color = tint,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
