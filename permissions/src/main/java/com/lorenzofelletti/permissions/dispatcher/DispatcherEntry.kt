package com.lorenzofelletti.permissions.dispatcher

import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcher
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcherDsl

/**
 * Represents an entry of a [RequestResultsDispatcher] object.
 * It contains the permissions associated to the request, and the actions to be dispatched
 * in case the permissions are granted or denied.
 */
class DispatcherEntry : PermissionDispatcher() {
    /**
     * The permissions associated to this entry
     */
    lateinit var permissions: Array<out String>
        private set

    /**
     * The action to be dispatched if the permissions are granted.
     */
    var onGranted: () -> Unit = {}
        private set

    /**
     * The action to be dispatched if the permissions are denied.
     */
    var onDenied: () -> Unit = {}
        private set

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

    companion object {
        /**
         * Adds the permissions related to this entry.
         */
        @PermissionDispatcherDsl
        fun DispatcherEntry.checkPermissions(permissions: Array<out String>) {
            this.permissions = permissions
        }

        /**
         * Adds a callback that will be called when the permission is granted.
         * The actions that you want to perform when the permission is granted should be added here.
         */
        @PermissionDispatcherDsl
        fun DispatcherEntry.doOnGranted(onGranted: () -> Unit) {
            this.onGranted = onGranted
        }

        /**
         * Adds a callback that will be called when the permission is denied.
         * The actions that you want to perform when the permission is denied should be added here.
         */
        @PermissionDispatcherDsl
        fun DispatcherEntry.doOnDenied(onDenied: () -> Unit) {
            this.onDenied = onDenied
        }
    }
}