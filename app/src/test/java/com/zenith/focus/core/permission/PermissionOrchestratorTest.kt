package com.zenith.focus.core.permission

import org.junit.Assert.*
import org.junit.Test

class PermissionOrchestratorTest {

    @Test
    fun testCoreOperationalWhenAccessibilityGranted() {
        val state = PermissionOrchestratorState(
            isAccessibilityGranted = true,
            isDeviceAdminGranted = false,
            isOverlayGranted = false,
            isInstallUnknownAppsGranted = false
        )
        assertTrue("When accessibility is granted, core should be operational", state.isCoreOperational)
    }

    @Test
    fun testCoreNotOperationalWhenAccessibilityDenied() {
        val state = PermissionOrchestratorState(
            isAccessibilityGranted = false,
            isDeviceAdminGranted = true,
            isOverlayGranted = true,
            isInstallUnknownAppsGranted = true
        )
        assertFalse("When accessibility is denied, core cannot be operational even if others are granted", state.isCoreOperational)
    }

    @Test
    fun testDefaultStateHasAllPermissionsDenied() {
        val state = PermissionOrchestratorState()
        assertFalse(state.isAccessibilityGranted)
        assertFalse(state.isDeviceAdminGranted)
        assertFalse(state.isOverlayGranted)
        assertFalse(state.isInstallUnknownAppsGranted)
        assertFalse(state.isCoreOperational)
    }

    @Test
    fun testCapabilityEnumCompleteness() {
        val capabilities = Capability.values().toSet()
        assertTrue(capabilities.contains(Capability.ACCESSIBILITY))
        assertTrue(capabilities.contains(Capability.DEVICE_ADMIN))
        assertTrue(capabilities.contains(Capability.OVERLAY))
        assertTrue(capabilities.contains(Capability.INSTALL_UNKNOWN_APPS))
        assertEquals(4, capabilities.size)
    }
}
