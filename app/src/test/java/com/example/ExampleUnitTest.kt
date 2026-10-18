package com.example

import com.example.data.model.ClonedInstanceEntity
import com.example.data.model.SocialPlatformCatalog
import com.example.sandbox.SessionSandboxEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun `verify multi-instance namespace and injection script generation`() {
    assertTrue(SocialPlatformCatalog.platforms.size >= 14)

    val firstIg = ClonedInstanceEntity(
      id = 10L,
      platformId = "instagram",
      platformName = "Instagram",
      monogram = "IG",
      instanceName = "Personal",
      badgeLabel = "#1",
      accountHandle = "@personal",
      workspaceGroup = "Personal",
      baseUrl = "https://www.instagram.com/",
      lastVisitedUrl = "https://www.instagram.com/",
      accentColorHex = 0xFF00F0FFL,
    )
    val secondIg = firstIg.copy(id = 11L, instanceName = "Creator", badgeLabel = "#2")

    val script1 = SessionSandboxEngine.buildSandboxInjectionScript(firstIg)
    val script2 = SessionSandboxEngine.buildSandboxInjectionScript(secondIg)

    assertNotEquals(script1, script2)
    assertTrue(script1.contains("__ts_ns_10_"))
    assertTrue(script2.contains("__ts_ns_11_"))
    assertFalse(SessionSandboxEngine.isTrackerUrl("https://www.instagram.com/"))
  }
}
