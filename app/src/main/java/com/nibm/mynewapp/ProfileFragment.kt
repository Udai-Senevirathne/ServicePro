package com.nibm.mynewapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnEditSave: MaterialButton
    private lateinit var btnDeletePicture: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isEditing = false
    private var selectedImageUri: Uri? = null
    private var currentProfilePicturePath: String? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                // Copy the image to internal storage and get the local path
                val localPath = saveImageToInternalStorage(uri, auth.currentUser?.uid ?: "temp_user")
                if (localPath != null) {
                    selectedImageUri = Uri.fromFile(File(localPath))
                    Glide.with(this)
                        .load(selectedImageUri)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(ivProfilePicture)
                    btnDeletePicture.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "Failed to save image locally", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for image access
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Permission denied to access images", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply fade-in animation to the entire view
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        view.startAnimation(fadeIn)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views with null checks
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture) ?: throw IllegalStateException("ivProfilePicture not found in layout")
        etName = view.findViewById(R.id.etName) ?: throw IllegalStateException("etName not found in layout")
        etEmail = view.findViewById(R.id.etEmail) ?: throw IllegalStateException("etEmail not found in layout")
        etPhone = view.findViewById(R.id.etPhone) ?: throw IllegalStateException("etPhone not found in layout")
        btnEditSave = view.findViewById(R.id.btnEditSave) ?: throw IllegalStateException("btnEditSave not found in layout")
        btnDeletePicture = view.findViewById(R.id.btnDeletePicture) ?: throw IllegalStateException("btnDeletePicture not found in layout")
        btnLogout = view.findViewById(R.id.btnLogout) ?: throw IllegalStateException("btnLogout not found in layout")
        progressBar = view.findViewById(R.id.progressBar) ?: throw IllegalStateException("progressBar not found in layout")

        // Load user profile from Firestore
        loadUserProfile()

        // Handle profile picture click to select image
        ivProfilePicture.setOnClickListener {
            if (isEditing) {
                requestImagePermission()
            }
        }

        // Handle delete profile picture
        btnDeletePicture.setOnClickListener {
            deleteProfilePicture()
        }

        // Handle Edit/Save button click
        btnEditSave.setOnClickListener {
            if (isEditing) {
                saveProfile()
            } else {
                enableEditing()
            }
        }

        // Handle Logout button click
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            // Navigate to login screen (replace with your login activity)
            val intent = Intent(requireContext(), LoginScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "You must be logged in to view your profile", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document != null && document.exists()) {
                    etName.setText(document.getString("name") ?: "")
                    etEmail.setText(document.getString("email") ?: "")
                    etPhone.setText(document.getString("phone") ?: "")
                    currentProfilePicturePath = document.getString("profilePicturePath")
                    if (!currentProfilePicturePath.isNullOrEmpty()) {
                        val file = File(currentProfilePicturePath)
                        if (file.exists()) {
                            Glide.with(this)
                                .load(file)
                                .error(R.drawable.ic_profile_placeholder)
                                .into(ivProfilePicture)
                            btnDeletePicture.visibility = View.VISIBLE
                        } else {
                            ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                            btnDeletePicture.visibility = View.GONE
                        }
                    } else {
                        ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                        btnDeletePicture.visibility = View.GONE
                    }
                } else {
                    // If no profile exists, prefill with Firebase Auth data (if available)
                    etEmail.setText(auth.currentUser?.email ?: "")
                    ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                    btnDeletePicture.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Real-time listener for profile updates
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    etName.setText(snapshot.getString("name") ?: "")
                    etEmail.setText(snapshot.getString("email") ?: "")
                    etPhone.setText(snapshot.getString("phone") ?: "")
                    currentProfilePicturePath = snapshot.getString("profilePicturePath")
                    if (!currentProfilePicturePath.isNullOrEmpty()) {
                        val file = File(currentProfilePicturePath)
                        if (file.exists()) {
                            Glide.with(this)
                                .load(file)
                                .error(R.drawable.ic_profile_placeholder)
                                .into(ivProfilePicture)
                            btnDeletePicture.visibility = View.VISIBLE
                        } else {
                            ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                            btnDeletePicture.visibility = View.GONE
                        }
                    } else {
                        ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                        btnDeletePicture.visibility = View.GONE
                    }
                } else {
                    // Handle case where the document no longer exists
                    etName.setText("")
                    etEmail.setText(auth.currentUser?.email ?: "")
                    etPhone.setText("")
                    ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                    btnDeletePicture.visibility = View.GONE
                    currentProfilePicturePath = null
                }
            }
    }

    private fun enableEditing() {
        isEditing = true
        etName.isEnabled = true
        etEmail.isEnabled = true
        etPhone.isEnabled = true
        ivProfilePicture.isClickable = true
        btnEditSave.text = "Save Profile"
        btnDeletePicture.visibility = if (currentProfilePicturePath != null || selectedImageUri != null) View.VISIBLE else View.GONE
    }

    private fun saveProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "You must be logged in to save your profile", Toast.LENGTH_SHORT).show()
            return
        }

        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            etName.error = "Name is required"
            return
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Valid email is required"
            return
        }
        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            return
        }

        showLoading(true)

        // If an image was selected, use the local path; otherwise, use the existing path
        val profilePicturePath = if (selectedImageUri != null) {
            selectedImageUri?.let { uri ->
                uri.path ?: currentProfilePicturePath
            }
        } else {
            currentProfilePicturePath
        }

        saveProfileToFirestore(name, email, phone, profilePicturePath)
    }

    private fun saveProfileToFirestore(name: String, email: String, phone: String, profilePicturePath: String?) {
        val userId = auth.currentUser?.uid ?: return

        val userProfile = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "lastUpdated" to System.currentTimeMillis(),
            "profilePicturePath" to profilePicturePath
        )

        db.collection("users").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show()
                isEditing = false
                etName.isEnabled = false
                etEmail.isEnabled = false
                etPhone.isEnabled = false
                ivProfilePicture.isClickable = false
                btnEditSave.text = "Edit Profile"
                selectedImageUri = null
                currentProfilePicturePath = profilePicturePath
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        showLoading(true)

        // Delete the local file if it exists
        if (!currentProfilePicturePath.isNullOrEmpty()) {
            val file = File(currentProfilePicturePath!!)
            if (file.exists()) {
                file.delete()
            }
        }

        // Update Firestore to remove the profile picture path
        db.collection("users").document(userId)
            .update("profilePicturePath", null)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Profile picture deleted", Toast.LENGTH_SHORT).show()
                ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                selectedImageUri = null
                currentProfilePicturePath = null
                btnDeletePicture.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveImageToInternalStorage(uri: Uri, userId: String): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(requireContext(), "Failed to access image", Toast.LENGTH_SHORT).show()
                return null
            }

            // Create a file in the app's internal storage
            val file = File(requireContext().filesDir, "profile_pictures_$userId.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving image: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Request READ_MEDIA_IMAGES
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 12 and below: Request READ_EXTERNAL_STORAGE
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}