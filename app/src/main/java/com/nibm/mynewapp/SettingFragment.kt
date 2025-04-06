package com.nibm.mynewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingFragment : Fragment() {

    private lateinit var ratingBar: RatingBar
    private lateinit var etComment: TextInputEditText
    private lateinit var btnSubmitRating: MaterialButton
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        ratingBar = view.findViewById(R.id.ratingBar)
        etComment = view.findViewById(R.id.etComment)
        btnSubmitRating = view.findViewById(R.id.btnSubmitRating)

        // Handle submit button click
        btnSubmitRating.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val rating = ratingBar.rating
        val comment = etComment.text.toString().trim()
        val userId = auth.currentUser?.uid

        // Validation
        if (rating == 0f) {
            Toast.makeText(requireContext(), "Please provide a rating", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(requireContext(), "You must be logged in to submit feedback", Toast.LENGTH_SHORT).show()
            return
        }

        // Create feedback object
        val feedback = hashMapOf(
            "userId" to userId,
            "rating" to rating,
            "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )

        // Save to Firestore "feedback" collection
        db.collection("feedback")
            .add(feedback)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(requireContext(), "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                etComment.text?.clear()
                ratingBar.rating = 0f
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error submitting feedback: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}