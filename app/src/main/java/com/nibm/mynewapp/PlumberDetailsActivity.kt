package com.nibm.mynewapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton

class PlumberDetailsActivity : AppCompatActivity() {

    private var plumberPhone: String? = null
    private var plumberName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plumber_details)

        // Get data from intent
        val plumber = intent.getParcelableExtra<Plumber>("PLUMBER_DATA")

        // Initialize views
        val imageView = findViewById<ImageView>(R.id.detail_plumber_image)
        val nameTextView = findViewById<TextView>(R.id.detail_plumber_name)
        val ratingBar = findViewById<RatingBar>(R.id.detail_plumber_rating)
        val specialtyTextView = findViewById<TextView>(R.id.detail_plumber_specialty)
        val introTextView = findViewById<TextView>(R.id.detail_plumber_intro)
        val phoneTextView = findViewById<TextView>(R.id.detail_plumber_phone)
        val emailTextView = findViewById<TextView>(R.id.detail_plumber_email)
        val callButton = findViewById<MaterialButton>(R.id.call_button)
        val messageButton = findViewById<MaterialButton>(R.id.message_button)

        // Set data
        plumber?.let {
            imageView.setImageResource(it.imageResId)
            nameTextView.text = it.name
            ratingBar.rating = it.rating
            specialtyTextView.text = "Specialty: ${it.specialty}"
            introTextView.text = it.intro
            phoneTextView.text = "Phone: ${it.phone}"
            emailTextView.text = "Email: ${it.email}"

            // Store phone and name locally
            plumberPhone = it.phone
            plumberName = it.name

            // Call button click listener
            callButton.setOnClickListener {
                plumberPhone?.let { phone ->
                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    try {
                        startActivity(callIntent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "No dialer app available", Toast.LENGTH_SHORT).show()
                    }
                } ?: Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
            }

            // Message button click listener
            messageButton.setOnClickListener {
                plumberPhone?.let { phone ->
                    plumberName?.let { name ->
                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:$phone")
                            putExtra("sms_body", "Hello $name, I need plumbing assistance.")
                        }
                        try {
                            startActivity(smsIntent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "No messaging app available", Toast.LENGTH_SHORT).show()
                        }
                    } ?: Toast.makeText(this, "Name not available", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
            }
        }
    }
}