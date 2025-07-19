package com.inhouse.client

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import com.inhouse.client.models.SDKConfig

class TrackingSDKTest {

    @Test
    fun testShortLinkDetection() {
        val detector = ShortLinkDetector("yourdomain.com")

        assertTrue(detector.isShortLink("https://yourdomain.com/abc123"))
        assertTrue(detector.isShortLink("https://sub.yourdomain.com/abc123"))
        assertFalse(detector.isShortLink("https://otherdomain.com/abc123"))
    }

    @Test
    fun testEventCreation() {
        val config = SDKConfig(
            projectToken = "test_token",
            shortLinkDomain = "test.com"
        )

        val storageManager = mock(StorageManager::class.java)
        `when`(storageManager.getDeviceId()).thenReturn("test_device_id")

        val networkClient = mock(NetworkClient::class.java)
        val eventTracker = EventTracker(networkClient, storageManager, config)

        // Test would verify event creation and tracking
    }
}