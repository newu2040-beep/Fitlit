package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.UserProfileEntity
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
    fun `permission utils returns expected permissions list`() {
        val permissions = PermissionUtils.getRequiredPermissions()
        assertTrue(permissions.contains(android.Manifest.permission.CAMERA))
        assertTrue(permissions.isNotEmpty())
    }
}
