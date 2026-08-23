package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.theme.FitlitThemeMode
import com.example.util.AvatarManager
import com.example.util.PermissionUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Fitlit", appName)
    }

    @Test
    fun `user profile entity default values`() {
        val profile = UserProfileEntity(
            name = "Rahul",
            goal = "Fat Loss",
            targetCalories = 1900,
            targetProtein = 140
        )
        assertEquals("Rahul", profile.name)
        assertEquals("Fat Loss", profile.goal)
        assertEquals(1900, profile.targetCalories)
        assertEquals(140, profile.targetProtein)
        assertTrue(profile.targetCalories > 1000)
    }

    @Test
    fun `theme mode enumeration and fromId parsing`() {
        assertEquals(FitlitThemeMode.AMOLED_BLACK, FitlitThemeMode.fromId("AMOLED_BLACK"))
        assertEquals(FitlitThemeMode.LIGHT, FitlitThemeMode.fromId("LIGHT"))
        assertEquals(FitlitThemeMode.CYBER_BLUE, FitlitThemeMode.fromId("CYBER_BLUE"))
        assertEquals(FitlitThemeMode.SYSTEM, FitlitThemeMode.fromId("unknown_mode"))
    }

    @Test
    fun `avatar manager base64 encoding and decoding`() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val base64 = AvatarManager.bitmapToBase64(bitmap)
        assertTrue(base64.isNotEmpty())
        val decoded = AvatarManager.base64ToBitmap(base64)
        assertNotNull(decoded)
    }

    @Test
    fun `permission utils returns expected permissions list`() {
        val permissions = PermissionUtils.getRequiredPermissions()
        assertTrue(permissions.contains(android.Manifest.permission.CAMERA))
        assertTrue(permissions.isNotEmpty())
    }

    @Test
    fun `reset action types have descriptions and confirm messages`() {
        val actions = com.example.ui.components.ResetActionType.values()
        assertEquals(4, actions.size)
        assertTrue(actions.any { it.name == "CLEAR_DEMO_TRACKING" })
        assertTrue(actions.any { it.name == "FACTORY_RESET" })
        val factoryReset = com.example.ui.components.ResetActionType.FACTORY_RESET
        assertTrue(factoryReset.isDestructive)
        assertTrue(factoryReset.title.isNotEmpty())
        assertTrue(factoryReset.confirmMessage.isNotEmpty())
    }
}
