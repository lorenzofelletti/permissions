@file:JvmName("PermissionDispatcherDslContainer")

package com.lorenzofelletti.permissions.dispatcher.dsl

import android.app.AlertDialog
import android.app.Dialog
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher

@DslMarker
annotation class PermissionDispatcherDsl

@PermissionDispatcherDsl
abstract class PermissionDispatcher

/**
 * Adds the permissions related to this entry.
 */
@PermissionDispatcherDsl
infix fun DispatcherEntry.checkPermissions(permissions: Array<out String>) {
    this.permissions = permissions
}

@JvmName("checkPermissionsVararg")
@PermissionDispatcherDsl
fun DispatcherEntry.checkPermissions(vararg permissions: String) {
    this.permissions = arrayOf(*permissions)
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

/**
 * Allows to show a rationale to the user when it is needed for at least one of the permissions.
 *
 * Use this method if [DispatcherEntry.showRationaleDialog] does not fit your needs.
 *
 * @param onShowRationale
 */
@PermissionDispatcherDsl
fun DispatcherEntry.rationale(onShowRationale: (List<String>, Int) -> Unit) {
    this.onShowRationale = onShowRationale
}

/**
 * Shows a rationale dialog to the user.
 * If the user clicks on the positive button, the permission request will be performed,
 * otherwise a custom action will be performed (if specified).
 *
 * @param message the message to be shown
 * @param positiveButtonText the text of the positive button
 * @param negativeButtonText the text of the negative button
 * @param onNegativeButtonPressed the action to be performed when the negative button is pressed
 */
@PermissionDispatcherDsl
fun DispatcherEntry.showRationaleDialog(
    message: String,
    positiveButtonText: String = "OK",
    negativeButtonText: String = "Cancel",
    onNegativeButtonPressed: (() -> Unit) = {}
) {
    rationale { _, _ ->
        manager.activity.runOnUiThread {
            AlertDialog.Builder(manager.activity).setMessage(message)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    manager.checkRequestAndDispatch(requestCode, comingFromRationale = true)
                }.setNegativeButton(negativeButtonText) { _, _ ->
                    onNegativeButtonPressed.invoke()
                }.show()
        }
    }
}

/**
 * Sets the [AlertDialog] to be shown when the rationale is needed.
 *
 * By  using this method, you can customize the dialog as you want, but do not call [Dialog.show] on
 * it, as it will be called automatically, just call [Dialog.create] instead.
 * Moreover, it is your responsibility to call the permission request when the positive button is
 * pressed.
 *
 * Example (Kotlin):
 * ```
 * val dialog = AlertDialog.Builder(this).setMessage("Message").
 *     setPositiveButton("Proceed") {
 *         manager.checkRequestAndDispatch(requestCode, comingFromRationale = true)
 *     }.create()
 * ```
 *
 * @param dialog the dialog to be shown
 */
@PermissionDispatcherDsl
fun DispatcherEntry.showRationaleDialog(
    dialog: AlertDialog,
) {
    rationale { _, _ ->
        manager.activity.runOnUiThread {
            dialog.show()
        }
    }
}

/**
 * Adds a new [DispatcherEntry] to the dispatcher.
 *
 * Example:
 * ```
 * val manager = PermissionManager(this)
 * manager buildRequestResultsDispatcher {
 *   withRequestCode(0) {
 *     checkPermissions("permission1", "permission2")
 *     showRationaleDialog("These permissions are needed for feature X")
 *     doOnGranted { ... }
 *     doOnDenied { ... }
 * }
 * ```
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
 * Replaces an entry's onGranted action in the dispatcher if present, otherwise it does nothing.
 *
 * @param requestCode The request code associated to the entry
 * @param onGranted The new onGranted action
 */
@PermissionDispatcherDsl
fun RequestResultsDispatcher.replaceEntryOnGranted(
    requestCode: Int, onGranted: () -> Unit
) {
    entries[requestCode]?.doOnGranted(onGranted)
}

/**
 * Replaces an entry's onDenied action in the dispatcher if present, otherwise it does nothing.
 *
 * @param requestCode The request code associated to the entry
 * @param onDenied The new onDenied action
 */
@PermissionDispatcherDsl
fun RequestResultsDispatcher.replaceEntryOnDenied(requestCode: Int, onDenied: () -> Unit) {
    entries[requestCode]?.doOnDenied(onDenied)
}

/**
 * Replaces an entry in the dispatcher if present, otherwise adds it.
 *
 * @param requestCode The request code associated to the entry
 * @param init A lambda that initializes the entry
 */
@PermissionDispatcherDsl
fun RequestResultsDispatcher.replaceEntry(
    requestCode: Int, init: DispatcherEntry.() -> Unit
) {
    entries[requestCode] = DispatcherEntry(manager, requestCode).apply(init)
}