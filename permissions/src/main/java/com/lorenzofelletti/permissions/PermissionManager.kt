package com.lorenzofelletti.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher
import com.lorenzofelletti.permissions.dispatcher.dsl.PermissionDispatcherDsl


/**
 * A class that manages the permissions of an [Activity].
 *
 * Use its [buildRequestResultsDispatcher] method to build a [RequestResultsDispatcher] that will
 * be used to dispatch the results of the permission requests.
 *
 * Then, use its [checkRequestAndDispatch] method to check if the permissions are granted and
 * dispatch the appropriate action.
 *
 * Finally, call its [dispatchOnRequestPermissionsResult] method from the [Activity]'s
 * [Activity.onRequestPermissionsResult] method to dispatch the results of the permission requests.
 *
 * @param activity The [Activity] that needs to manage the permissions
 */
class PermissionManager(val activity: Activity) {
    lateinit var dispatcher: RequestResultsDispatcher
        private set

    /**
     * Builds a [RequestResultsDispatcher] object that is used to dispatch the results of the
     * permission requests to the appropriate actions.
     *
     * @param init A lambda that initializes the [RequestResultsDispatcher] object.
     */
    @PermissionDispatcherDsl
    fun buildRequestResultsDispatcher(init: RequestResultsDispatcher.() -> Unit) {
        dispatcher = RequestResultsDispatcher(this).apply(init)
    }

    /**
     * Checks if the permissions are granted, and dispatches the appropriate action based on the
     * results.
     *
     * If some permissions are not granted, the user is asked to grant them.
     *
     * If all the permissions are already granted, the action associated to onGranted in the
     * [RequestResultsDispatcher] is dispatched.
     *
     * @param requestCode The request code associated to the permissions
     */
    fun checkRequestAndDispatch(requestCode: Int, comingFromRationale: Boolean = false) {
        val permissions = dispatcher.getPermissions(requestCode)
            ?: throw UnhandledRequestCodeException(requestCode)
        val permissionsNotGranted = permissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                activity, permission
            ) != PackageManager.PERMISSION_GRANTED

        }.toTypedArray()

        if (permissionsNotGranted.isEmpty()) {
            // All permissions are granted
            dispatcher.getOnGranted(requestCode)?.invoke()
        } else {
            // Some permissions are not granted
            val shouldShowRationale = permissionsNotGranted.any { permission ->
                shouldShowRequestPermissionRationale(activity, permission)
            }

            if (shouldShowRationale && !comingFromRationale) {
                dispatchRationale(permissionsNotGranted, requestCode)
            } else {
                ActivityCompat.requestPermissions(activity, permissionsNotGranted, requestCode)
            }
        }
    }

    private fun dispatchRationale(permissionsNotGranted: Array<out String>, requestCode: Int) {
        val toInvoke = dispatcher.getOnShowRationale(requestCode) ?: fun(
            _: List<String>,
            requestCode: Int
        ) {
            checkRequestAndDispatch(requestCode, true)
        }
        toInvoke.invoke(permissionsNotGranted.toList(), requestCode)
    }

    /**
     * Checks whether a set of permissions is granted or not.
     *
     * @param permissions The permissions to be checked
     *
     * @return true if all permissions are granted, false otherwise
     */
    fun checkPermissionsGranted(permissions: Array<out String>): Boolean {
        for (permission in permissions) {
            if (!checkPermissionGranted(activity, permission)) return false
        }
        return true
    }

    /**
     * Checks whether the permissions for a given request code are granted or not.
     * Throws an [UnhandledRequestCodeException] if the request code is not handled.
     *
     * @param requestCode The request code associated to the permissions to be checked
     * @return true if all permissions are granted, false otherwise
     */
    fun checkPermissionsGranted(requestCode: Int): Boolean {
        val permissions = dispatcher.getPermissions(requestCode)
            ?: throw UnhandledRequestCodeException(requestCode)
        return checkPermissionsGranted(permissions)
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
        dispatcher.dispatchAction(requestCode, grantResults)?.invoke()
    }

    /**
     * Checks whether a permission is granted in the context.
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
}

/**
 * Exception thrown when the request code is not handled by the [RequestResultsDispatcher] object.
 */
class UnhandledRequestCodeException(requestCode: Int) : Throwable() {
    override val message: String =
        "Request code $requestCode is not handled by the RequestResultsDispatcher object. " +
                "Please add a withRequestCode block to the buildRequestResultsDispatcher function."
}
