package com.zenith.focus.core.permission

import org.junit.Assert.*
import org.junit.Test

class OemNavigationManagerTest {

    @Test
    fun testBrandDetectionXiaomi() {
        assertEquals(DeviceBrand.XIAOMI, OemNavigationManager.detectDeviceBrand(manufacturer = "Xiaomi", brand = "Xiaomi"))
        assertEquals(DeviceBrand.XIAOMI, OemNavigationManager.detectDeviceBrand(manufacturer = "Xiaomi", brand = "Redmi"))
        assertEquals(DeviceBrand.XIAOMI, OemNavigationManager.detectDeviceBrand(manufacturer = "POCO", brand = "POCO"))
    }

    @Test
    fun testBrandDetectionSamsung() {
        assertEquals(DeviceBrand.SAMSUNG, OemNavigationManager.detectDeviceBrand(manufacturer = "samsung", brand = "samsung"))
        assertEquals(DeviceBrand.SAMSUNG, OemNavigationManager.detectDeviceBrand(manufacturer = "Samsung", brand = "Galaxy"))
    }

    @Test
    fun testBrandDetectionOppoRealmeOnePlus() {
        assertEquals(DeviceBrand.OPPO_REALME_ONEPLUS, OemNavigationManager.detectDeviceBrand(manufacturer = "OnePlus", brand = "OnePlus"))
        assertEquals(DeviceBrand.OPPO_REALME_ONEPLUS, OemNavigationManager.detectDeviceBrand(manufacturer = "OPPO", brand = "OPPO"))
        assertEquals(DeviceBrand.OPPO_REALME_ONEPLUS, OemNavigationManager.detectDeviceBrand(manufacturer = "Realme", brand = "Realme"))
    }

    @Test
    fun testBrandDetectionVivo() {
        assertEquals(DeviceBrand.VIVO, OemNavigationManager.detectDeviceBrand(manufacturer = "vivo", brand = "vivo"))
        assertEquals(DeviceBrand.VIVO, OemNavigationManager.detectDeviceBrand(manufacturer = "iQOO", brand = "iQOO"))
    }

    @Test
    fun testBrandDetectionTranssion() {
        assertEquals(DeviceBrand.TRANSSION, OemNavigationManager.detectDeviceBrand(manufacturer = "INFINIX", brand = "Infinix"))
        assertEquals(DeviceBrand.TRANSSION, OemNavigationManager.detectDeviceBrand(manufacturer = "TECNO", brand = "Tecno"))
        assertEquals(DeviceBrand.TRANSSION, OemNavigationManager.detectDeviceBrand(manufacturer = "itel", brand = "itel"))
    }

    @Test
    fun testBrandDetectionGeneric() {
        assertEquals(DeviceBrand.GENERIC, OemNavigationManager.detectDeviceBrand(manufacturer = "Google", brand = "Pixel"))
        assertEquals(DeviceBrand.GENERIC, OemNavigationManager.detectDeviceBrand(manufacturer = "motorola", brand = "moto"))
        assertEquals(DeviceBrand.GENERIC, OemNavigationManager.detectDeviceBrand(manufacturer = "unknown", brand = "unknown"))
    }

    @Test
    fun testGuidanceCompleteness() {
        for (brand in DeviceBrand.values()) {
            val guidance = OemNavigationManager.getGuidance(brand)
            assertNotNull(guidance.step1Title)
            assertTrue(guidance.step1Title.isNotEmpty())
            assertNotNull(guidance.step2Title)
            assertTrue(guidance.step2Title.isNotEmpty())
            assertEquals(brand, guidance.brand)
        }
    }
}
