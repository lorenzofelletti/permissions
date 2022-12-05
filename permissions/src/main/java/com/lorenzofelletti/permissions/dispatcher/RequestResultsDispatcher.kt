package com.lorenzofelletti.permissions.dispatcher

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.rationale
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcher
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcherDsl

/**
 * A dispatcher for the results of permission requests.
 */
class RequestResultsDispatcher(private val manager: PermissionManager) : PermissionDispatcher() {
    private val entries: MutableMap<Int, DispatcherEntry> = mutableMapOf()

    fun dispatchAction(requestCode: Int, grantResults: IntArray): (() -> Unit)? =
        when (checkGrantResults(grantResults)) {
            true -> entries[requestCode]?.onGranted
            false -> entries[requestCode]?.onDenied
        }

    internal fun getPermissions(requestCode: Int): Array<out String>? =
        entries[requestCode]?.permissions

    internal fun getOnGranted(requestCode: Int): (() -> Unit)? = entries[requestCode]?.onGranted

    internal fun getOnShowRationale(requestCode: Int): ((List<String>, Int) -> Unit)? =
        entries[requestCode]?.onShowRationale

    /**
     * Checks the results of a permission request
     *
     * @param grantResults The results of the permission request
     *
     * @return true if all permissions were granted, false otherwise
     */
    private fun checkGrantResults(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    companion object {
        /**
         * Adds a new [DispatcherEntry] to the dispatcher.
         *
         * @param requestCode The request code associated to the entry
         * @param init A lambda that initializes the entry
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.withRequestCode(
            requestCode: Int, init: DispatcherEntry.() -> Unit
        ) {
            entries[requestCode] = DispatcherEntry(manager, requestCode).apply(init)
        }
    }
}