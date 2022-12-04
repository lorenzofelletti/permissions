package com.lorenzofelletti.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionsUtilities {
    private lateinit var requestResultsDispatcher: RequestResultsDispatcher

    /**
     * Class used to dispatch the results of the permission requests.
     * @param onGrantedMap A map of the permissions to be granted and the callback to be called when
     * the permission is granted.
     * @param onDeniedMap A map of the permissions to be denied and the callback to be called when
     * the permission is denied.
     */
    data class RequestResultsDispatcher(
        val onGrantedMap: Map<Int, () -> Unit>, val onDeniedMap: Map<Int, () -> Unit>
    ) {
        /**
         * Adds a callback to a permission request code to be called when the permission is granted.
         * It can be used inside [buildRequestResultsDispatcher] to add a callback to a permission
         * request code.
         */
        val onGranted = fun RequestResultsDispatcher.(requestCode: Int, onGranted: () -> Unit) {
            onGrantedMap + (requestCode to onGranted)
        }

        /**
         * Adds a callback to a permission request code to be called when the permission is denied.
         * It can be used inside [buildRequestResultsDispatcher] to add a callback to a permission
         * request code.
         */
        val onDenied = fun RequestResultsDispatcher.(requestCode: Int, onDenied: () -> Unit) {
            onDeniedMap + (requestCode to onDenied)
        }

        /**
         * Returns the callback to be called based on the permission request code and the result of
         * the permission request. It can be null if the permission request code is not in the maps.
         *
         * @param requestCode The permission request code.
         * @param grantResults The result of the permission request.
         * @return The callback to be called
         */
        fun dispatchedAction(requestCode: Int, grantResults: IntArray): (() -> Unit)? =
            when (checkGrantResults(grantResults)) {
                true -> onGrantedMap[requestCode]
                false -> onDeniedMap[requestCode]
            }
    }

    /**
     * Builds a [RequestResultsDispatcher] object that can be used to dispatch the results of the
     * permission requests.
     *
     * @param init A lambda that initializes the [RequestResultsDispatcher] object.
     */
    fun buildRequestResultsDispatcher(init: RequestResultsDispatcher.() -> Unit) {
        requestResultsDispatcher = RequestResultsDispatcher(
            onGrantedMap = mapOf(), onDeniedMap = mapOf()
        ).apply { init() }
    }

    /**
     * Checks for a set of permissions. If some are not granted, the user is asked to grant them.
     *
     * @param activity The activity that is requesting the permissions
     * @param permissions The permissions to be checked
     * @param requestCode The request code to be used when requesting the permissions
     */
    fun checkPermissions(
        activity: Activity, permissions: Array<out String>, requestCode: Int
    ) {
        val permissionsNotGranted = permissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                activity.baseContext, permission
            ) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsNotGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNotGranted, requestCode)
        } else {
            // all permissions granted
        }
    }

    /**
     * Checks whether a set of permissions is granted or not.
     *
     * @param context The context to be used for checking the permissions
     * @param permissions The permissions to be checked
     *
     * @return true if all permissions are granted, false otherwise
     */
    fun checkPermissionsGranted(context: Context, permissions: Array<out String>): Boolean {
        for (permission in permissions) {
            if (!checkPermissionGranted(context, permission)) return false
        }
        return true
    }

    /**
     * Checks the result of a permission request, and dispatches the appropriate action.
     * The actions can be defined by the user by using the [buildRequestResultsDispatcher] function
     *
     * @param requestCode The request code of the permission request
     * @param grantResults The results of the permission request
     */
    fun dispatchOnRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
    ) {
        requestResultsDispatcher.dispatchedAction(requestCode, grantResults)?.invoke()
    }

    /**
     * Checks whether a permission is granted in the context
     *
     * @param context The context to be used for checking the permission
     * @param permission The permission to be checked
     *
     * @return true if the permission is granted, false otherwise
     */
    private fun checkPermissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
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