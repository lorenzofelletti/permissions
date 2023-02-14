# Permissions
[![](https://jitpack.io/v/lorenzofelletti/permissions.svg)](https://jitpack.io/#lorenzofelletti/permissions)

## Easy Permissions Management Library for Android

An easy to use permissions management library written in Kotlin.

### Import the Library
The library is deployed on JitPack [here](https://jitpack.io/#lorenzofelletti/permissions).
To add it to your project, add to `settings.gradle`:
```Groovy
dependencyResolutionManagement {
  ...
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
And to your module level build.gradle:
```Groovy
dependencies {
  ...
  implementation 'com.github.lorenzofelletti:permissions:0.4.2'
}
```

### Usage
To use the library in your project, just:
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
  permissionManager checkRequestAndDispatch POSITION_REQUEST_CODE
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

## Contributing
All kinds of contributions are welcome (bug reports, feature requests, pull requests, etc.).
Suggestions and improvements on documentation, tests, code quality, translations, etc. are also welcome.
