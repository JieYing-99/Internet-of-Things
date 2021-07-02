package com.example.flashlight

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    val databaseRef = FirebaseDatabase.getInstance().reference

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Flash Light"
        val isFlashAvailable = applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        if (!isFlashAvailable) {
            showNoFlashError()
        }
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        cameraId = cameraManager.cameraIdList[0]

        databaseRef.child("flash").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val flash = dataSnapshot.value.toString()
                Log.e("Flash value", "$flash")

                if (flash == "1") {
                    switchFlashLight(true)
                } else {
                    switchFlashLight(false)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Error", "Failed to read value.", error.toException())
            }
        })
    }

    private fun showNoFlashError() {
        val alert = AlertDialog.Builder(this)
                .create()
                alert.setTitle("Error ")
                alert.setMessage("Flash is not available in this device.")
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { _, _ -> finish() }
                alert.show()
    }

    private fun switchFlashLight(status: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, status)
            }
    }
}