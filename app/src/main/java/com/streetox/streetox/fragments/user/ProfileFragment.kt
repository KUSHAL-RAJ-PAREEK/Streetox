package com.streetox.streetox.fragments.user

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.activities.MainActivity

import com.streetox.streetox.adapters.ProfileCdAdapter
import com.streetox.streetox.databinding.FragmentProfileBinding
import com.streetox.streetox.models.user
import com.streetox.streetox.room.UserProfile
import com.streetox.streetox.room.UserProfileDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var email: String
    lateinit var db: UserProfileDao.AppDatabase


    private var bottomNavigationView: BottomNavigationView? = null

    var tabTitle = arrayOf("As Customer", "As Courier")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = Room.databaseBuilder(
            requireContext(),
            UserProfileDao.AppDatabase::class.java, "app-database"
        ).build()

        binding = FragmentProfileBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        email = auth.currentUser?.email.toString()

        val uid = auth.currentUser?.uid

        if (uid != null) {
            GlobalScope.launch(Dispatchers.IO) {
                val userProfile = db.userProfileDao().getUserProfile(uid)
                if (userProfile == null || userProfile.profileImageUri.isNullOrEmpty()) {
                    // Fetch image URI from Firebase Storage and save to Room
                    fetchImageFromFirebase(uid)
                } else {
                    // Load image from Room and set it to the main image view
                    loadImageFromRoom(userProfile.profileImageUri)
                }
            }
        }



        val pager = binding.profileViewpager
        val tab = binding.profileTabLayout
        pager.adapter = ProfileCdAdapter(childFragmentManager, lifecycle)

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.VISIBLE

        TabLayoutMediator(tab, pager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()


        setUserData()

        changePassword()

        click_on_edit_profile()

        setUserProfile()


        logout()

        return binding.root
    }

    private fun fetchImageFromFirebase(userId: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images").child("$userId.jpg")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            // Save image URI to Room
            saveImageToRoom(userId, imageUrl)
        }.addOnFailureListener { exception ->
            // Handle failures
            Log.e("EditProfileFragment", "Failed to fetch image from Firebase: $exception")
        }
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
                }
            }
        }
    }

    private fun saveImageToRoom(userId: String, imageUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val existingProfile = db.userProfileDao().getUserProfile(userId)
            if (existingProfile != null) {
                // Update the existing profile image URI
                existingProfile.profileImageUri = imageUrl
                // Update the existing profile in the database
                db.userProfileDao().update(existingProfile)
            } else {
                // If the user doesn't have an existing profile, insert a new one
                val userProfile = UserProfile(userId = userId, profileImageUri = imageUrl)
                db.userProfileDao().insert(userProfile)
            }
            // Load image from Room and set it to the main image view
            loadImageFromRoom(imageUrl)
        }
    }



    private fun loadImageFromRoom(imageUri: String) {
        GlobalScope.launch(Dispatchers.Main) { // Switch to the main thread
            if (isAdded) {
                Glide.with(requireContext())
                    .load(imageUri)
                    .into(binding.mainImg)
            }
        }
    }


    private fun click_on_edit_profile() {
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    private fun changePassword() {
        binding.changePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }
    }

    private fun logout() {
        binding.logout.setOnClickListener {
            showCustomDialogBox()
        }
    }


    private fun showCustomDialogBox() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.fragment_logout_dailog_box)

        // Blur background
        val window = dialog.window
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        val logoutBtn = dialog.findViewById<Button>(R.id.logout_btn)
        val cancelBtn = dialog.findViewById<Button>(R.id.logout_cancel_btn)

        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireActivity(), MainActivity::class.java))
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setUserData() {
        database = FirebaseDatabase.getInstance().getReference("Users")
        if (email.isNotEmpty()) {
            getUserData()
        }
    }



    private fun getUserData() {
        val key = auth.currentUser?.uid
        if (key != null) {
            database.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(user::class.java)
                    if (user != null) {
                        binding.userName.text = user.name
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
}