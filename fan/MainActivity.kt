package com.example.fan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide

val databaseRef = FirebaseDatabase.getInstance().reference

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Fan"

        databaseRef.child("fan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val fanValue = dataSnapshot.value.toString()
                Log.e("Fan value", "$fanValue")

                if (fanValue == "1") {
                    showGif()
                } else {
                    showImg()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Error", "Failed to read value.", error.toException())
            }
        })

    }

    fun showGif() {
        val imageView: ImageView = findViewById(R.id.imageView)
        Glide.with(this).load(R.drawable.fan).into(imageView)
    }

    fun showImg() {
        val imageView: ImageView = findViewById(R.id.imageView)
        Glide.with(this).load(R.drawable.fan_img).into(imageView)
    }
}