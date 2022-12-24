package com.lorenzofelletti.permissions.dispatcher

import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcher


/**
 * Represents an entry of a [RequestResultsDispatcher] object.
 * It contains the permissions associated to the request, and the actions to be dispatched
 * in case the permissions are granted or denied.
 */
class DispatcherEntry(
    internal val manager: PermissionManager, val requestCode: Int
) : PermissionDispatcher() {
    /**
     * The permissions associated to this entry
     */
    lateinit var permissions: Array<out String>
        internal set

    /**
     * The action to be dispatched if the permissions are granted.
     */
    var onGranted: () -> Unit = {}
        internal set

    /**
     * The action to be dispatched if the permissions are denied.
     */
    var onDenied: () -> Unit = {}
        internal set

    /**
     * The rationale to be shown
     */
    var onShowRationale: ((List<String>, requestCode: Int) -> Unit)? = null
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DispatcherEntry

        if (!permissions.contentEquals(other.permissions)) return false
        if (onGranted != other.onGranted) return false
        if (onDenied != other.onDenied) return false

        return true
    }

    override fun hashCode(): Int {
        var result = permissions.contentHashCode()
        result = 31 * result + onGranted.hashCode()
        result = 31 * result + onDenied.hashCode()
        return result
    }
}