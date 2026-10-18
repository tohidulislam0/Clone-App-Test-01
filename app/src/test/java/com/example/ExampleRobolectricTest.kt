package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ClonedInstanceEntity
import com.example.sandbox.SessionSandboxEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read TwinSpace app name and verify sandbox isolation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TwinSpace", appName)

    val cloneA = ClonedInstanceEntity(
      id = 1L,
      platformId = "instagram",
      platformName = "Instagram",
      monogram = "IG",
      instanceName = "Personal",
      badgeLabel = "#1",
      accountHandle = "@user1",
      workspaceGroup = "Personal",
      baseUrl = "https://www.instagram.com/",
      lastVisitedUrl = "https://www.instagram.com/",
      accentColorHex = 0xFF00F0FFL,
    )
    val cloneB = cloneA.copy(id = 2L, instanceName = "Studio", badgeLabel = "#2")

    assertNotEquals(cloneA.sandboxNamespace, cloneB.sandboxNamespace)
    assertNotEquals(cloneA.deviceFingerprintHash, cloneB.deviceFingerprintHash)

    val sanitized = SessionSandboxEngine.sanitizeTrackingParams(
      url = "https://www.instagram.com/reel/123/?igshid=abc&utm_source=share&keep=1",
      enabled = true,
    )
    assertEquals("https://www.instagram.com/reel/123/?keep=1", sanitized)
    assertTrue(SessionSandboxEngine.isTrackerUrl("https://google-analytics.com/collect"))
  }
}
