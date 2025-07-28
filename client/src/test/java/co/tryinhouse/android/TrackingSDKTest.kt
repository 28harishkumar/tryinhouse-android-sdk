package co.tryinhouse.android

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import co.tryinhouse.android.models.SDKConfig

class TrackingSDKTest {

    @Test
    fun testSDKConfig() {
        val config = SDKConfig(
            projectId = "test_project_id",
            projectToken = "test_token",
            shortLinkDomain = "test.com",
            serverUrl = "https://test.com",
            enableDebugLogging = false
        )

        assertEquals("test_project_id", config.projectId)
        assertEquals("test_token", config.projectToken)
        assertEquals("test.com", config.shortLinkDomain)
        assertEquals("https://test.com", config.serverUrl)
        assertFalse(config.enableDebugLogging)
    }

    @Test
    fun testEventCreation() {
        val config = SDKConfig(
            projectId = "test_project_id",
            projectToken = "test_token",
            shortLinkDomain = "test.com",
            serverUrl = "https://test.com",
            enableDebugLogging = false
        )

        val storageManager = mock(StorageManager::class.java)
        `when`(storageManager.getDeviceId()).thenReturn("test_device_id")

        val networkClient = mock(NetworkClient::class.java)
        val eventTracker = EventTracker(networkClient, storageManager, config)

        // Test would verify event creation and tracking
        assertNotNull(eventTracker)
    }
}