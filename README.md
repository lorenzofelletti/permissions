# Permissions
[![](https://jitpack.io/v/lorenzofelletti/permissions.svg)](https://jitpack.io/#lorenzofelletti/permissions)

## Easy permissions management library for Android

An easy to use permissions management library designed to be used with Kotlin.

The library is deployed on JitPack here, follow the steps indicated in the link to add it to your project.

To use it in your project, just:
* Declare the permissions your app will use in your application's Manifest
  * Example: add to the app `AndroidManifest.xml`
  ```xml
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  ```
* Create a `PermissionManager` in your `Activity`
  * Example: in your `MainActivity`
  ```Kotlin
    private val permissionManager = PermissionManager(this)
    ```
* Decide a request code for each set of permissions that you will require simultaneously (each request code is a positive integer of your choice, but do not use the same code for different set of permissions)
  * Example: add to your `MainActivity`
  ```Kotlin
  companion object {
        private const val POSITION_REQUEST_CODE = 1
        private val POSITION_REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
  ```
* Build the RequestResultsDispatcher
  * Example: in the `MainActivity`'s `onCreate` function add
  ```Kotlin
  permissionManager.buildRequestResultsDispatcher {
      withRequestCode(POSITION_REQUEST_CODE) {
          checkPermissions(POSITION_REQUIRED_PERMISSIONS)
          doOnGranted {
              Log.d(TAG, "Location permission granted") 
          }
          doOnDenied {
              Log.d(TAG, "Location permission denied")
          }
      }
  }
  ```
* Call `permissionManager.checkRequestAndDispatch` where you want to check for a set of permissions (and ask them if not granted)
  * Example: in your Activity, where you want to check (and request) permissions add
  ```Kotlin
  permissionManager.checkRequestAndDispatch(
            POSITION_REQUEST_CODE
        )
  ```
* Override `onRequestPermissionsResult` and call `PermissionsUtilities.dispatchOnRequestPermissionsResult` in it
  * Example: inside your `MainActivity`
  ```Kotlin
  override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.dispatchOnRequestPermissionsResult(requestCode, grantResults)
    }
  ```
