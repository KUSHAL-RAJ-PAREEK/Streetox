package com.streetox.streetox.fragments.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentCustomerBinding
import com.streetox.streetox.models.user


class CustomerFragment : Fragment() {

    private lateinit var binding: FragmentCustomerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var User : user
    private lateinit var email : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCustomerBinding.inflate(layoutInflater)
        database = FirebaseDatabase.getInstance().getReference("Users")
        auth = FirebaseAuth.getInstance()
        email = auth.currentUser?.email.toString()


        set_user_email_and_phone_number()

        database.keepSynced(true)

        return binding.root
    }



    private fun set_user_email_and_phone_number(){

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.correct)

        val key = auth.currentUser?.uid
        if (key != null) {
            database.child(key).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    User = snapshot.getValue(user::class.java)!!


                    binding.checkerEmail.apply {
                        setImageDrawable(drawable)
                        requestLayout()
                    }

                    binding.customerEmail.text = (User.email)

                    if(User.phone_number != null){
                        //check icon
                        binding.checkerEmail.apply {
                            setImageDrawable(drawable)
                            requestLayout()
                        }
                        binding.customerPhoneNumber.text = User.phone_number
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.showToast(requireContext(),"unable to fetch data")
                }

            })
        }
    }
}