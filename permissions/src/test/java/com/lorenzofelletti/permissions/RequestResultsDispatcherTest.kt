package com.lorenzofelletti.permissions

import android.app.Activity
import android.content.pm.PackageManager
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.checkPermissions
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.doOnGranted
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher.Companion.addEntry
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher.Companion.removeEntry
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher.Companion.replaceEntry
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher.Companion.withRequestCode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RequestResultsDispatcherTest {
    private lateinit var permissionManager: PermissionManager
    private lateinit var dispatcher: RequestResultsDispatcher

    @Before
    fun setUp() {
        val mockActivity = mock(Activity::class.java)
        permissionManager = PermissionManager(mockActivity)

        permissionManager.buildRequestResultsDispatcher {}

        dispatcher = permissionManager.dispatcher
    }

    @Test
    fun `test add entry`() {
        val dispatcher = RequestResultsDispatcher(permissionManager)
        val entry = DispatcherEntry(permissionManager, 0)

        dispatcher.addEntry(entry)

        assert(dispatcher.entries.size == 1)
    }

    @Test
    fun `test add entry declarative`() {
        permissionManager.dispatcher.addEntry(0) {}

        assert(dispatcher.entries.size == 1)
    }

    @Test
    fun `test remove entry`() {
        val dispatcher = RequestResultsDispatcher(permissionManager)
        val entry = DispatcherEntry(permissionManager, 0)

        dispatcher.addEntry(entry)
        dispatcher.removeEntry(entry)

        assert(dispatcher.entries.isEmpty())
    }

    @Test
    fun `test remove entry by request code`() {
        val dispatcher = permissionManager.dispatcher
        val entry = DispatcherEntry(permissionManager, 0)

        dispatcher.addEntry(entry)
        dispatcher.removeEntry(0)

        assert(dispatcher.entries.isEmpty())
    }

    @Test
    fun `test replace on existing entry`() {
        var testVar = 0

        val permissionManager = permissionManagerWithEntryZero()

        permissionManager.dispatcher.replaceEntry(0) {
            checkPermissions(arrayOf("permission1"))
            doOnGranted { testVar = 2 }
        }

        permissionManager.dispatcher.dispatchAction(
            0,
            intArrayOf(PackageManager.PERMISSION_GRANTED)
        )?.invoke()

        assert(testVar == 2)
    }

    @Test
    fun `test dispatch on granted`() {
        var testVar = 0
        val entry = DispatcherEntry(permissionManager, 0)

        entry.doOnGranted { testVar = 1 }

        dispatcher.addEntry(entry)
        dispatcher.dispatchAction(
            0,
            arrayOf(PackageManager.PERMISSION_GRANTED).toIntArray()
        )?.invoke()

        assert(testVar == 1)
    }

    private fun permissionManagerWithEntryZero(): PermissionManager {
        val permissionManager = PermissionManager(mock(Activity::class.java))
        permissionManager.buildRequestResultsDispatcher {
            withRequestCode(0) {
                checkPermissions(arrayOf("permission0"))
                doOnGranted { }
            }
        }

        return permissionManager
    }
}