package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismViolet

@Composable
fun TwinSpaceHeroBanner(
  totalClones: Int,
  multiInstancePlatforms: Int,
  totalTrackersBlocked: Int,
  onCreateCloneClick: () -> Unit,
  onLaunchDualViewClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          colors = listOf(
            ElectricCyan.copy(alpha = 0.55f),
            PrismViolet.copy(alpha = 0.45f),
            Color.Transparent,
          )
        ),
        shape = RoundedCornerShape(24.dp),
      ),
    color = ObsidianSurfaceElevated,
    tonalElevation = 6.dp,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .drawBehind {
          // Ambient cyber radial glow + overlapping twin sandbox frames
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(ElectricCyan.copy(alpha = 0.16f), Color.Transparent),
              center = Offset(size.width * 0.85f, size.height * 0.25f),
              radius = size.width * 0.55f,
            ),
            center = Offset(size.width * 0.85f, size.height * 0.25f),
            radius = size.width * 0.55f,
          )
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(PrismViolet.copy(alpha = 0.18f), Color.Transparent),
              center = Offset(size.width * 0.15f, size.height * 0.85f),
              radius = size.width * 0.5f,
            ),
            center = Offset(size.width * 0.15f, size.height * 0.85f),
            radius = size.width * 0.5f,
          )
          // Decorative twin prism frames on right side
          val frameW = 72.dp.toPx()
          val frameH = 92.dp.toPx()
          val rightX = size.width - frameW - 20.dp.toPx()
          val topY = 18.dp.toPx()
          drawRoundRect(
            color = PrismViolet.copy(alpha = 0.22f),
            topLeft = Offset(rightX + 12.dp.toPx(), topY),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
          )
          drawRoundRect(
            color = ElectricCyan.copy(alpha = 0.28f),
            topLeft = Offset(rightX - 6.dp.toPx(), topY + 14.dp.toPx()),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
          )
        }
        .padding(20.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            color = ElectricCyan.copy(alpha = 0.14f),
            shape = RoundedCornerShape(50),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Sandbox engine status",
                tint = ElectricCyan,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "MULTI-INSTANCE SANDBOX ENGINE",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Parallel Social Accounts.\nZero Cross-Contamination.",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Each clone runs in a dedicated CookieJar, virtualized DOM storage namespace, and isolated device fingerprint.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Telemetry pill strip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          HeroMetricTile(
            value = totalClones.toString(),
            label = "Active Clones",
            accentColor = ElectricCyan,
            modifier = Modifier.weight(1f),
          )
          HeroMetricTile(
            value = multiInstancePlatforms.toString(),
            label = "Multi-Account Apps",
            accentColor = PrismViolet,
            modifier = Modifier.weight(1f),
          )
          HeroMetricTile(
            value = totalTrackersBlocked.toString(),
            label = "Trackers Shielded",
            accentColor = NeonEmerald,
            modifier = Modifier.weight(1f),
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onCreateCloneClick,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("hero_create_clone_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricCyan,
              contentColor = ObsidianBg,
            ),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Clone new instance",
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Clone Social App",
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          FilledTonalButton(
            onClick = onLaunchDualViewClick,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("hero_dual_split_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = PrismViolet.copy(alpha = 0.22f),
              contentColor = Color.White,
            ),
          ) {
            Icon(
              imageVector = Icons.Filled.VerticalSplit,
              contentDescription = "Dual split view",
              tint = PrismViolet,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Dual Split View",
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HeroMetricTile(
  value: String,
  label: String,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(ObsidianBg.copy(alpha = 0.65f))
      .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp),
  ) {
    Text(
      text = value,
      style = MaterialTheme.typography.titleLarge,
      color = accentColor,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun InstanceAvatarBadge(
  monogram: String,
  badgeLabel: String,
  accentColorHex: Long,
  hasPinLock: Boolean = false,
  size: Dp = 52.dp,
  modifier: Modifier = Modifier,
) {
  val accentColor = Color(accentColorHex or 0xFF000000L)
  Box(modifier = modifier.size(size + 6.dp), contentAlignment = Alignment.Center) {
    Box(
      modifier = Modifier
        .size(size)
        .clip(RoundedCornerShape(16.dp))
        .background(
          Brush.linearGradient(
            colors = listOf(
              accentColor.copy(alpha = 0.26f),
              ObsidianBg,
            )
          )
        )
        .border(
          width = 1.8.dp,
          color = accentColor.copy(alpha = 0.85f),
          shape = RoundedCornerShape(16.dp),
        ),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = monogram.take(2).uppercase(),
        style = MaterialTheme.typography.titleLarge.copy(
          fontSize = if (size >= 48.dp) 18.sp else 14.sp,
          fontWeight = FontWeight.Bold,
        ),
        color = Color.White,
      )
    }

    // Bottom-right clone instance badge (#1, #2, ALT, BIZ)
    Surface(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .offset(x = 2.dp, y = 2.dp),
      shape = RoundedCornerShape(6.dp),
      color = accentColor,
      shadowElevation = 4.dp,
    ) {
      Text(
        text = badgeLabel.take(4),
        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = ObsidianBg,
      )
    }

    if (hasPinLock) {
      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .size(18.dp)
          .clip(CircleShape)
          .background(ObsidianBg)
          .border(1.dp, accentColor, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Lock,
          contentDescription = "PIN Protected Clone",
          tint = accentColor,
          modifier = Modifier.size(10.dp),
        )
      }
    }
  }
}
