package com.lorenzofelletti.permissionsexample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.*

class MainActivity : AppCompatActivity() {
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)

        permissionManager buildRequestResultsDispatcher {
            withRequestCode(POSITION_REQUEST_CODE) {
                checkPermissions(POSITION_REQUIRED_PERMISSIONS)
                showRationaleDialog(message = "Location permission is required to use this feature")
                doOnGranted {
                    Log.d(TAG, "Location permission granted")
                }
                doOnDenied {
                    Log.d(TAG, "Location permission denied")
                }
            }
        }

        permissionManager checkRequestAndDispatch POSITION_REQUEST_CODE

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            permissionManager.checkRequestAndDispatch(
                POSITION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.dispatchOnRequestPermissionsResult(requestCode, grantResults)
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