package com.streetox.streetox.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentCustomerBinding
import com.streetox.streetox.databinding.FragmentDeliveryBinding
import com.streetox.streetox.models.user

class DeliveryFragment : Fragment() {

    private lateinit var binding: FragmentDeliveryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var User : user
    private lateinit var email : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDeliveryBinding.inflate(layoutInflater)
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

                    binding.deliveryEmail.text = (User.email)

                    if(User.phone_number != null){
                        //check icon
                        binding.checkerEmail.apply {
                            setImageDrawable(drawable)
                            requestLayout()
                        }
                        binding.deliveryPhoneNumber.text = User.phone_number
                    }else{
                        verify_phone_number()
                    }
                }

                private fun verify_phone_number(){
                    binding.deliveryPhoneNumber.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_profileFragment_to_verifyPhone_NumberFragment
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.showToast(requireContext(),"unable to fetch data")
                }

            })
        }
    }
}