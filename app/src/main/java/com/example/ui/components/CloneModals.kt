package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.SocialPlatform
import com.example.data.model.SocialPlatformCatalog
import com.example.data.model.UserAgentMode
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InstanceColors
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateOrEditCloneDialog(
  initialPlatform: SocialPlatform?,
  editingInstance: ClonedInstanceEntity?,
  existingCloneCountForPlatform: Int,
  onDismiss: () -> Unit,
  onCreateClone: (
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
  ) -> Unit,
  onUpdateClone: (ClonedInstanceEntity) -> Unit,
  onDeleteClone: ((ClonedInstanceEntity) -> Unit)? = null,
) {
  val isEditing = editingInstance != null
  val defaultPlatform = initialPlatform ?: SocialPlatformCatalog.platforms.first()
  val nextCloneNumber = existingCloneCountForPlatform + 1

  var selectedPlatform by remember {
    mutableStateOf(
      if (editingInstance != null) {
        SocialPlatformCatalog.findById(editingInstance.platformId) ?: defaultPlatform
      } else {
        defaultPlatform
      }
    )
  }

  var isCustomUrlMode by remember {
    mutableStateOf(
      editingInstance != null && SocialPlatformCatalog.findById(editingInstance.platformId) == null
    )
  }

  var customPlatformName by remember {
    mutableStateOf(editingInstance?.platformName ?: "Custom Social")
  }
  var instanceName by remember {
    mutableStateOf(
      editingInstance?.instanceName ?: "Account #$nextCloneNumber"
    )
  }
  var badgeLabel by remember {
    mutableStateOf(
      editingInstance?.badgeLabel ?: "#$nextCloneNumber"
    )
  }
  var accountHandle by remember {
    mutableStateOf(
      editingInstance?.accountHandle ?: "@account_$nextCloneNumber"
    )
  }
  var workspaceGroup by remember {
    mutableStateOf(editingInstance?.workspaceGroup ?: "Personal")
  }
  var baseUrl by remember {
    mutableStateOf(editingInstance?.baseUrl ?: selectedPlatform.defaultUrl)
  }
  var accentColorHex by remember {
    mutableLongStateOf(
      editingInstance?.accentColorHex
        ?: InstanceColors[nextCloneNumber % InstanceColors.size]
    )
  }
  var userAgentMode by remember {
    mutableStateOf(
      editingInstance?.let { UserAgentMode.fromName(it.userAgentMode) }
        ?: UserAgentMode.ANDROID_MOBILE
    )
  }
  var privacyTrackerBlock by remember {
    mutableStateOf(editingInstance?.privacyTrackerBlock ?: true)
  }
  var spoofDeviceFingerprint by remember {
    mutableStateOf(editingInstance?.spoofDeviceFingerprint ?: true)
  }
  var forceDarkWebMode by remember {
    mutableStateOf(editingInstance?.forceDarkWebMode ?: false)
  }
  var incognitoOnExit by remember {
    mutableStateOf(editingInstance?.incognitoOnExit ?: false)
  }
  var pinCode by remember {
    mutableStateOf(editingInstance?.pinCode ?: "")
  }
  var quickNotes by remember {
    mutableStateOf(editingInstance?.quickNotes ?: "")
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
      color = ObsidianSurface,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Title Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = if (isEditing) "Configure Cloned Sandbox" else "Create Isolated Social Clone",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_clone_modal_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close modal",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Platform selector if creating new clone
        if (!isEditing) {
          Text(
            text = "1. SELECT TARGET SOCIAL PLATFORM",
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            SocialPlatformCatalog.platforms.take(8).forEach { platform ->
              val selected = !isCustomUrlMode && selectedPlatform.id == platform.id
              FilterChip(
                selected = selected,
                onClick = {
                  isCustomUrlMode = false
                  selectedPlatform = platform
                  baseUrl = platform.defaultUrl
                },
                label = { Text(platform.name) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ElectricCyan.copy(alpha = 0.22f),
                  selectedLabelColor = ElectricCyan,
                ),
              )
            }
            FilterChip(
              selected = isCustomUrlMode,
              onClick = { isCustomUrlMode = true },
              label = { Text("Custom URL") },
            )
          }
        }

        if (isCustomUrlMode) {
          OutlinedTextField(
            value = customPlatformName,
            onValueChange = { customPlatformName = it },
            label = { Text("App / Network Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = instanceName,
            onValueChange = { instanceName = it },
            label = { Text("Clone Label (e.g. Work, Alt)") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("clone_name_input"),
          )
          OutlinedTextField(
            value = badgeLabel,
            onValueChange = { badgeLabel = it.take(4).uppercase() },
            label = { Text("Badge") },
            singleLine = true,
            modifier = Modifier
              .width(96.dp)
              .testTag("clone_badge_input"),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = accountHandle,
            onValueChange = { accountHandle = it },
            label = { Text("Account Handle (@username)") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("clone_handle_input"),
          )
          OutlinedTextField(
            value = pinCode,
            onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(4) },
            label = { Text("4-Digit PIN") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
              .width(120.dp)
              .testTag("clone_pin_input"),
          )
        }

        OutlinedTextField(
          value = baseUrl,
          onValueChange = { baseUrl = it },
          label = { Text("Target Social Login / Feed URL") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("clone_url_input"),
        )

        // Workspace Group Selector
        Text(
          text = "WORKSPACE GROUP & ACCENT RING",
          style = MaterialTheme.typography.labelLarge,
          color = ElectricCyan,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SocialPlatformCatalog.workspaceGroups.filter { it != "All" }.forEach { group ->
            FilterChip(
              selected = workspaceGroup == group,
              onClick = { workspaceGroup = group },
              label = { Text(group) },
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          InstanceColors.forEach { colorHex ->
            val c = Color(colorHex or 0xFF000000L)
            val isSelected = accentColorHex == colorHex
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(c)
                .border(
                  width = if (isSelected) 3.dp else 1.dp,
                  color = if (isSelected) Color.White else ObsidianBg,
                  shape = CircleShape,
                )
                .clickable { accentColorHex = colorHex },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Selected color",
                  tint = ObsidianBg,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // User-Agent Profile Spoofing
        Text(
          text = "ISOLATED USER-AGENT PROFILE",
          style = MaterialTheme.typography.labelLarge,
          color = ElectricCyan,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          UserAgentMode.entries.forEach { mode ->
            FilterChip(
              selected = userAgentMode == mode,
              onClick = { userAgentMode = mode },
              label = { Text(mode.shortTag) },
            )
          }
        }

        // Sandbox Isolation Toggles
        SandboxToggleRow(
          title = "Per-Instance Device Fingerprint Spoofing",
          subtitle = "Injects unique Canvas & hardwareConcurrency signature per clone ID",
          checked = spoofDeviceFingerprint,
          onCheckedChange = { spoofDeviceFingerprint = it },
        )
        SandboxToggleRow(
          title = "Block Cross-App Trackers & Strip UTM/fbclid",
          subtitle = "Prevents social platforms from linking your parallel accounts",
          checked = privacyTrackerBlock,
          onCheckedChange = { privacyTrackerBlock = it },
        )
        SandboxToggleRow(
          title = "Ephemeral Incognito Session",
          subtitle = "Automatically wipe cookies when closing this clone",
          checked = incognitoOnExit,
          onCheckedChange = { incognitoOnExit = it },
        )

        OutlinedTextField(
          value = quickNotes,
          onValueChange = { quickNotes = it },
          label = { Text("Account Vault Notes (Bio snippets, Hashtags, Backup notes)") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("clone_notes_input"),
          maxLines = 2,
        )

        // Submit Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (isEditing && editingInstance != null && onDeleteClone != null) {
            OutlinedButton(
              onClick = { onDeleteClone(editingInstance) },
              modifier = Modifier
                .height(48.dp)
                .testTag("modal_delete_clone_button"),
              border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAlert),
            ) {
              Icon(Icons.Filled.Delete, contentDescription = "Delete clone", tint = CrimsonAlert)
            }
          }

          OutlinedButton(
            onClick = {
              if (isEditing && editingInstance != null) {
                onUpdateClone(
                  editingInstance.copy(
                    instanceName = instanceName.trim().ifBlank { editingInstance.instanceName },
                    badgeLabel = badgeLabel.trim().ifBlank { "#1" },
                    accountHandle = accountHandle.trim().ifBlank { "@user" },
                    workspaceGroup = workspaceGroup,
                    baseUrl = baseUrl.trim(),
                    accentColorHex = accentColorHex,
                    userAgentMode = userAgentMode.name,
                    privacyTrackerBlock = privacyTrackerBlock,
                    spoofDeviceFingerprint = spoofDeviceFingerprint,
                    forceDarkWebMode = forceDarkWebMode,
                    incognitoOnExit = incognitoOnExit,
                    pinCode = pinCode,
                    quickNotes = quickNotes.trim(),
                  )
                )
              } else {
                val pId = if (isCustomUrlMode) "custom_${System.currentTimeMillis()}" else selectedPlatform.id
                val pName = if (isCustomUrlMode) customPlatformName else selectedPlatform.name
                val mono = if (isCustomUrlMode) customPlatformName.take(2).uppercase() else selectedPlatform.monogram
                onCreateClone(
                  pId,
                  pName,
                  mono,
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
                  false,
                )
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("save_clone_button"),
          ) {
            Text(if (isEditing) "Save Changes" else "Save to Workspace")
          }

          if (!isEditing) {
            Button(
              onClick = {
                val pId = if (isCustomUrlMode) "custom_${System.currentTimeMillis()}" else selectedPlatform.id
                val pName = if (isCustomUrlMode) customPlatformName else selectedPlatform.name
                val mono = if (isCustomUrlMode) customPlatformName.take(2).uppercase() else selectedPlatform.monogram
                onCreateClone(
                  pId,
                  pName,
                  mono,
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
                  true,
                )
              },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("create_and_launch_clone_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                contentColor = ObsidianBg,
              ),
            ) {
              Text("Clone & Run")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SandboxToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ObsidianSurfaceElevated)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = ObsidianBg,
        checkedTrackColor = ElectricCyan,
      ),
    )
  }
}

@Composable
fun PinUnlockDialog(
  instance: ClonedInstanceEntity,
  onDismiss: () -> Unit,
  onVerifyPin: (String) -> Boolean,
) {
  var enteredPin by remember { mutableStateOf("") }
  var hasError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ObsidianSurface,
    icon = {
      Icon(
        imageVector = Icons.Filled.Lock,
        contentDescription = "Locked clone",
        tint = ElectricCyan,
        modifier = Modifier.size(32.dp),
      )
    },
    title = {
      Text(
        text = "Unlock ${instance.platformName} (${instance.badgeLabel})",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Enter the 4-digit sandbox PIN for '${instance.instanceName}' (${instance.accountHandle}).",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
          value = enteredPin,
          onValueChange = {
            enteredPin = it.filter { ch -> ch.isDigit() }.take(4)
            hasError = false
          },
          label = { Text("4-Digit PIN") },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          singleLine = true,
          isError = hasError,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("pin_unlock_input"),
        )
        if (hasError) {
          Text(
            text = "Incorrect PIN. Hint: check instance quick notes if seeded (1234).",
            style = MaterialTheme.typography.bodySmall,
            color = CrimsonAlert,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!onVerifyPin(enteredPin)) {
            hasError = true
          }
        },
        modifier = Modifier.testTag("pin_unlock_confirm_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = ElectricCyan,
          contentColor = ObsidianBg,
        ),
      ) {
        Text("Unlock Sandbox")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}
