package com.lorenzofelletti.permissions

import android.app.Activity
import android.content.pm.PackageManager
import com.lorenzofelletti.permissions.dispatcher.dsl.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RequestResultsDispatcherTest {
    private lateinit var permissionManager: PermissionManager

    @Before
    fun setUp() {
        val mockActivity = mock(Activity::class.java)
        permissionManager = PermissionManager(mockActivity)

        permissionManager.buildRequestResultsDispatcher {}
    }

    @Test
    fun `test add entry`() {
        assert(permissionManager.dispatcher.entries.isEmpty())

        permissionManager.dispatcher.addEntry(0) {}

        assert(permissionManager.dispatcher.entries.size == 1)
    }

    @Test
    fun `test remove entry by request code`() {
        val permissionManager = permissionManagerWithEntryZero()
        permissionManager.dispatcher.removeEntry(0)

        assert(permissionManager.dispatcher.entries.isEmpty())
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
            0, intArrayOf(PackageManager.PERMISSION_GRANTED)
        )?.invoke()

        assert(testVar == 2)
    }

    @Test
    fun `test dispatch on granted`() {
        var testVar = 0
        permissionManager.dispatcher.addEntry(0) {
            doOnGranted { testVar = 1 }
        }

        permissionManager.dispatcher.dispatchAction(
            0, arrayOf(PackageManager.PERMISSION_GRANTED).toIntArray()
        )?.invoke()

        assert(testVar == 1)
    }

    @Test
    fun `test dispatch on denied`() {
        var testVar = 0

        permissionManager.dispatcher.addEntry(0) {
            doOnDenied { testVar = 1 }
        }
        permissionManager.dispatcher.dispatchAction(
            0, arrayOf(PackageManager.PERMISSION_DENIED).toIntArray()
        )?.invoke()

        assert(testVar == 1)
    }

    @Test
    fun `test replace entry`() {
        var testVar = 0

        val permissionManager = permissionManagerWithEntryZero()

        permissionManager.dispatcher.replaceEntry(0) {
            doOnGranted { testVar = 2 }
        }

        permissionManager.dispatcher.dispatchAction(
            0, intArrayOf(PackageManager.PERMISSION_GRANTED)
        )?.invoke()

        assert(testVar == 2)
    }

    @Test
    fun `test replace entry's on granted`() {
        val permissionManager = permissionManagerWithEntryZero()

        var testVar = 0

        permissionManager.dispatcher.replaceEntryOnGranted(0) {
            testVar = 2
        }

        permissionManager.dispatcher.dispatchAction(
            0, intArrayOf(PackageManager.PERMISSION_GRANTED)
        )?.invoke()

        assert(testVar == 2)
    }

    @Test
    fun `replace entry's on denied`() {
        val permissionManager = permissionManagerWithEntryZero()

        var testVar = 0

        permissionManager.dispatcher.replaceEntryOnDenied(0) {
            testVar = 2
        }

        permissionManager.dispatcher.dispatchAction(
            0, intArrayOf(PackageManager.PERMISSION_DENIED)
        )?.invoke()

        assert(testVar == 2)
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