package com.streetox.streetox.fragments.user

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.bumptech.glide.Glide
import com.facebook.appevents.AppEventsLogger
import com.facebook.appevents.codeless.internal.ViewHierarchy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.internal.Util
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentChangePasswordBinding
import com.streetox.streetox.databinding.FragmentEditProfileBinding
import com.streetox.streetox.models.user
import com.streetox.streetox.room.UserProfile
import com.streetox.streetox.room.UserProfileDao
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.HashMap

class EditProfileFragment : Fragment() {
    private var bottomNavigationView: BottomNavigationView? = null
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var email: String
    private lateinit var name: String
    private lateinit var User : user
    private lateinit var dob: String
    private var imageUri : Uri? = null
    lateinit var db: UserProfileDao.AppDatabase

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        startCrop(uri)
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let { bitmap ->
                imageUri = getImageUri(requireContext(), bitmap)
                startCrop(imageUri)
            }
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }


    private fun startCrop(uri: Uri?) {
        if (uri != null) {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setOutputCompressQuality(80)
                .start(requireContext(), this)
        } else {
            Toast.makeText(requireContext(), "Failed to pick image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val croppedUri = result.uri
                // Now you have the cropped image URI, you can use it as needed
                imageUri = croppedUri
                binding.mainImg.setImageURI(imageUri)
                if (imageUri != null) {
                    uploadImage()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                // Handle error
                Log.e("CropImage", "Failed to crop image: $error")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE
        email = auth.currentUser?.email.toString()
        db = Room.databaseBuilder(
            requireContext(),
            UserProfileDao.AppDatabase::class.java, "app-database"
        ).build()
        setUserData()
        on_dob_click()
        on_name_click()
        on_back_btn_click()
        on_email_click()
        on_number_click()
        on_img_edit_click()
        setUserProfile()
        database.keepSynced(true)
        return binding.root
    }

    private fun setUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            GlobalScope.launch(Dispatchers.IO) {
                val userProfile = db.userProfileDao().getUserProfile(uid)
                if (userProfile != null) {
                    withContext(Dispatchers.Main) {
                        loadImageFromRoom(userProfile.profileImageUri)
                    }
                } else {
                    Utils.showToast(requireContext(), "User profile not found")
                }
            }
        }
    }

    private fun loadImageFromRoom(imageUri: String) {
        Glide.with(requireContext())
            .load(imageUri)
            .into(binding.mainImg)
    }

    private fun on_back_btn_click() {
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        }
    }

    private fun on_name_click() {
        binding.nameTxt.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_updateNameFragment)
        }
    }

    private fun on_dob_click() {
        binding.dobTxt.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_updateDOBFragment)
        }
    }

    private fun setUserData() {
        if (email.isNotEmpty()) {
            getUserData()
        }
    }

    private fun getUserData() {
        val key = auth.currentUser?.uid
        if (key != null) {
            database.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val User = snapshot.getValue(user::class.java)
                    if (User != null) {
                        binding.nameTxt.text = User.name
                        binding.emailTxt.text = User.email
                        if (User.dob != null) {
                            binding.dobTxt.text = User.dob
                        } else {
                            binding.dobTxt.setText("DD/MM/YYYY")
                        }
                        if (User.phone_number != null) {
                            binding.numberTxt.text = User.phone_number
                        } else {
                            binding.numberTxt.text = "+91 0000000000"
                        }
                    } else {
                        Utils.showToast(requireContext(), "User data is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.showToast(requireContext(), "Unable to fetch user data")
                }
            })
        }
    }

    private fun on_img_edit_click() {
        binding.imgEditBtn.setOnClickListener {
            showImagePickerOptions()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Camera", "Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePictureFromCamera()
                    1 -> selectImageFromGallery()
                }
            }
            .show()
    }

    private fun takePictureFromCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePicture.launch(intent)
        }
    }

    private fun selectImageFromGallery() {
        selectImage.launch("image/*")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictureFromCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun on_email_click() {
        binding.emailTxt.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_updateEmailPasswordFragment)
        }
    }

    private fun on_number_click() {
        database = FirebaseDatabase.getInstance().getReference("Users")
        val number = auth.currentUser?.phoneNumber.toString()
        binding.numberTxt.setOnClickListener {
            if (number != "") {
                Utils.showToast(requireContext(), "phone number cannot be changed")
            } else {
                Utils.showToast(requireContext(), "please verify phone number")
            }
        }
    }

    private fun uploadImage() {
        if (imageUri != null) {
            val userId = auth.currentUser?.uid ?: return
            GlobalScope.launch(Dispatchers.IO) {
                val existingProfile = db.userProfileDao().getUserProfile(userId)
                if (existingProfile != null) {
                    // Update the existing profile image URI
                    existingProfile.profileImageUri = imageUri.toString()
                    // Update the existing profile in the database
                    db.userProfileDao().update(existingProfile)
                } else {
                    // If the user doesn't have an existing profile, insert a new one
                    val userProfile = UserProfile(userId = userId, profileImageUri = imageUri.toString())
                    db.userProfileDao().insert(userProfile)
                }
            }
            binding.mainImg.setImageURI(imageUri)
        } else {
            Utils.showToast(requireContext(), "Upload a nice picture")
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
