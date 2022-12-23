package com.lorenzofelletti.permissions

import android.app.Activity
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.doOnDenied
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.doOnGranted
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
    }

    @Test
    fun `test do on granted`() {
        var testVar = 0
        val entry = DispatcherEntry(permissionManager, 0)

        entry.doOnGranted { testVar = 1 }

        entry.onGranted()

        assert(testVar == 1)
    }

    @Test
    fun `test do on denied`() {
        var testVar = 0
        val entry = DispatcherEntry(permissionManager, 0)

        entry.doOnDenied { testVar = 1 }

        entry.onDenied()

        assert(testVar == 1)
    }
}