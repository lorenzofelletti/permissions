package com.lorenzofelletti.permissions

import android.app.Activity
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry
import com.lorenzofelletti.permissions.dispatcher.dsl.checkPermissions
import com.lorenzofelletti.permissions.dispatcher.dsl.doOnDenied
import com.lorenzofelletti.permissions.dispatcher.dsl.doOnGranted
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DispatcherEntryTest {
    private lateinit var permissionManager: PermissionManager

    @Before
    fun setUp() {
        val mockActivity = mock(Activity::class.java)
        permissionManager = PermissionManager(mockActivity)
        permissionManager buildRequestResultsDispatcher {}
    }

    @Test
    fun `test set entry permissions`() {
        val permissionsArray = arrayOf("permission1", "permission2")
        val entry = DispatcherEntry(permissionManager.dispatcher, 1)
        entry checkPermissions permissionsArray
        assert(entry.getPermissions().contentEquals(permissionsArray))
    }

    @Test
    fun `test set entry permissions with vararg`() {
        val permissionsArray = arrayOf("permission1", "permission2")
        val entry = DispatcherEntry(permissionManager.dispatcher, 1)
        entry.checkPermissions(permissionsArray[0], permissionsArray[1])
        assert(entry.getPermissions().contentEquals(permissionsArray))
    }

    @Test
    fun `test do on granted`() {
        var testVar = 0
        val entry = DispatcherEntry(permissionManager.dispatcher, 0)

        entry.doOnGranted { testVar = 1 }

        entry.onGranted()

        assert(testVar == 1)
    }

    @Test
    fun `test do on denied`() {
        var testVar = 0
        val entry = DispatcherEntry(permissionManager.dispatcher, 0)

        entry.doOnDenied { testVar = 1 }

        entry.onDenied()

        assert(testVar == 1)
    }
}