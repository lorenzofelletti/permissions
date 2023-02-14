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
 * Use the [buildRequestResultsDispatcher] method to build a [RequestResultsDispatcher] that will
 * be used to dispatch the results of the permission requests. Note that this is mandatory to
 * use this class correctly.
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
    /** The dispatcher that will be used to dispatch the results of the permission requests. */
    lateinit var dispatcher: RequestResultsDispatcher
        private set

    /**
     * Builds a [RequestResultsDispatcher] object that is used to dispatch the results of the
     * permission requests to the appropriate actions.
     *
     * Example:
     * ```
     * val manager = PermissionManager(this)
     * manager buildRequestResultsDispatcher {
     *   withRequestCode(0) {
     *     checkPermissions("permission1", "permission2")
     *     showRationaleDialog("Permissions are needed for feature X")
     *     doOnGranted { ... }
     *     doOnDenied { ... }
     *   }
     * }
     * ```
     *
     * @param init A lambda that initializes the [RequestResultsDispatcher] object.
     */
    @PermissionDispatcherDsl
    infix fun buildRequestResultsDispatcher(init: RequestResultsDispatcher.() -> Unit) {
        dispatcher = RequestResultsDispatcher(this)
        dispatcher.apply(init)
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
     *
     * @throws UnhandledRequestCodeException if the request code is not handled by the dispatcher
     */
    infix fun checkRequestAndDispatch(requestCode: Int) {
        checkRequestAndDispatch(requestCode, false)
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
     * Note: `comingFromRationale` is used internally to avoid showing the rationale dialog in loop.
     *
     * @param requestCode The request code associated to the permissions
     * @param comingFromRationale true if the method is called from the rationale, defaults to false
     *
     * @throws UnhandledRequestCodeException if the request code is not handled by the dispatcher
     */
    internal fun checkRequestAndDispatch(requestCode: Int, comingFromRationale: Boolean = false) {
        val permissionsNotGranted = dispatcher.getPermissions(requestCode)?.filter { permission ->
            ActivityCompat.checkSelfPermission(
                activity, permission
            ) != PackageManager.PERMISSION_GRANTED
        }?.toTypedArray() ?: throw UnhandledRequestCodeException(requestCode, activity)

        if (permissionsNotGranted.isEmpty()) {
            // All permissions are granted
            dispatcher.dispatchOnGranted(requestCode)
        } else {
            // Some permissions are not granted
            dispatchSomePermissionsNotGranted(
                permissionsNotGranted,
                requestCode,
                comingFromRationale
            )
        }
    }

    /**
     * Dispatches the case in which some permissions are not granted
     *
     * @param permissionsNotGranted
     * @param requestCode
     * @param comingFromRationale
     */
    private fun dispatchSomePermissionsNotGranted(
        permissionsNotGranted: Array<out String>,
        requestCode: Int,
        comingFromRationale: Boolean
    ) {
        // if not coming from rationale, gets the list of permissions that require rationale to be shown
        val permissionsRequiringRationale =
            if (!comingFromRationale) getPermissionsRequiringRationale(permissionsNotGranted) else emptyList()

        // if some permissions require rationale, show rationale, otherwise ask for permissions
        // Note: if coming from rationale, the list will be empty, so the else branch will be executed
        if (permissionsRequiringRationale.isNotEmpty()) {
            dispatcher.showRationale(requestCode, permissionsRequiringRationale)
        } else {
            ActivityCompat.requestPermissions(activity, permissionsNotGranted, requestCode)
        }
    }

    private fun getPermissionsRequiringRationale(permissionsNotGranted: Array<out String>) =
        permissionsNotGranted.filter { permission ->
            shouldShowRequestPermissionRationale(activity, permission)
        }.toList()

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

}

/**
 * Exception thrown when the request code is not handled by the [RequestResultsDispatcher] object.
 */
class UnhandledRequestCodeException(requestCode: Int, context: Context) : Throwable() {
    override val message: String =
        context.getString(R.string.unhandled_request_code_exception_message, requestCode)
}
