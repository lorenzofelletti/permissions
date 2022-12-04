package com.lorenzofelletti.permissions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionsUtilities.buildRequestResultsDispatcher {
            onGranted(POSITION_REQUEST_CODE) {
                // Do something
                Log.d(TAG, "onCreate: Position permission granted")
            }
            onDenied(POSITION_REQUEST_CODE) {
                // Do something else
                Log.d(TAG, "onCreate: Position permission denied")
            }
        }

        PermissionsUtilities.checkPermissions(
            this,
            POSITION_REQUIRED_PERMISSIONS,
            POSITION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtilities.dispatchOnRequestPermissionsResult(requestCode, grantResults)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val POSITION_REQUEST_CODE = 1
        private val POSITION_REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}