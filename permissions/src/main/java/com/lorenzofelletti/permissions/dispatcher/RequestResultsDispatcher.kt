package com.lorenzofelletti.permissions.dispatcher

import android.content.pm.PackageManager
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcher
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcherDsl

/**
 * A dispatcher for the results of permission requests.
 */
class RequestResultsDispatcher(private val manager: PermissionManager) : PermissionDispatcher() {
    /** Maps the entries of the dispatcher with the request codes. */
    var entries: MutableMap<Int, DispatcherEntry> = mutableMapOf()
        private set

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

        /**
         * Removes an entry from the dispatcher.
         *
         * @param requestCode The request code of the entry to be removed
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.removeEntry(requestCode: Int) {
            entries.remove(requestCode)
        }

        /**
         * Removes an entry from the dispatcher.
         *
         * @param entry The entry to be removed
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.removeEntry(entry: DispatcherEntry) {
            entries.remove(entry.requestCode)
        }

        /**
         * Adds a new entry to the dispatcher if it doesn't already exist.
         *
         * @param entry The entry to be added
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.addEntry(entry: DispatcherEntry) {
            if (entries[entry.requestCode] == null) {
                entries[entry.requestCode] = entry
            }
        }

        /**
         * Adds a new entry to the dispatcher if it doesn't already exist.
         *
         * @param requestCode The request code associated to the entry
         * @param init A lambda that initializes the entry
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.addEntry(requestCode: Int, init: DispatcherEntry.() -> Unit) {
            if (entries[requestCode] == null) {
                entries[requestCode] = DispatcherEntry(manager, requestCode).apply(init)
            }
        }

        /**
         * Replaces an entry in the dispatcher if present, otherwise adds it.
         *
         * @param entry The entry to be replaced or added
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.replaceEntry(entry: DispatcherEntry) {
            entries[entry.requestCode] = entry
        }

        /**
         * Replaces an entry in the dispatcher if present, otherwise adds it.
         *
         * @param requestCode The request code associated to the entry
         * @param init A lambda that initializes the entry
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.replaceEntry(requestCode: Int, init: DispatcherEntry.() -> Unit) {
            entries[requestCode] = DispatcherEntry(manager, requestCode).apply(init)
        }

        /**
         * Removes all entries from the dispatcher.
         */
        @PermissionDispatcherDsl
        fun RequestResultsDispatcher.clearEntries() {
            entries.clear()
        }
    }
}