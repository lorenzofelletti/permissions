package com.lorenzofelletti.permissions.dispatcher

import android.content.pm.PackageManager
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcher

/**
 * A dispatcher for the results of permission requests.
 */
class RequestResultsDispatcher(internal val manager: PermissionManager) : PermissionDispatcher() {
    /** Maps the entries of the dispatcher with the request codes. */
    internal var entries: MutableMap<Int, DispatcherEntry> = mutableMapOf()

    internal fun dispatchAction(requestCode: Int, grantResults: IntArray): (() -> Unit)? =
        when (checkGrantResults(grantResults)) {
            true -> entries[requestCode]?.onGranted
            false -> entries[requestCode]?.onDenied
        }

    internal fun getPermissions(requestCode: Int): Array<out String>? =
        entries[requestCode]?.getPermissions()

    internal fun dispatchOnGranted(requestCode: Int) {
        entries[requestCode]?.onGranted?.invoke()
    }

    /**
     * Shows the rationale for the permissions associated to the given request code, otherwise
     * it calls [PermissionManager.checkRequestAndDispatch] with `comingFromRationale = true`.
     *
     * @param requestCode The request code associated to the permissions
     * @param permissions The permissions requiring a rationale
     */
    internal fun showRationale(requestCode: Int, permissions: List<String>) {
        entries[requestCode]?.onShowRationale?.invoke(permissions, requestCode)
            ?: manager.checkRequestAndDispatch(requestCode, true)
    }

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
}