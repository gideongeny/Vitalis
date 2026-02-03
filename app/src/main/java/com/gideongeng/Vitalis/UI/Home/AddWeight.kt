package com.gideongeng.Vitalis.UI.Home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.gideongeng.Vitalis.Utils.Constant
import com.gideongeng.Vitalis.Utils.Constant.isInternetOn
import com.gideongeng.Vitalis.databinding.FragmentAddWeightBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class AddWeight : Fragment() {
    private lateinit var binding: FragmentAddWeightBinding
    private var userDitails: DocumentReference = FirebaseFirestore.getInstance().collection("user").document(
        FirebaseAuth.getInstance().currentUser!!.uid.toString()
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddWeightBinding.inflate(inflater, container, false)
        binding.apply {
            weight.minValue = 1
            weight.maxValue = 200
            ft.minValue = 3
            ft.maxValue = 8
            ft.wrapSelectorWheel = true
            inch.minValue = 1
            inch.maxValue = 12
            inch.wrapSelectorWheel = true
            add.setOnClickListener {
                if (isInternetOn(requireContext())) {
                    addweight()
                    
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser == null || currentUser.isAnonymous) {
                        // Guest user: skip Firestore sync check and go home
                        Toast.makeText(requireContext(), "Weight saved locally", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireActivity(), Home_screen::class.java))
                        requireActivity().finish()
                        return@setOnClickListener
                    }

                    // Authenticated user: Check if height exists in cloud
                    val detailsRef = FirebaseFirestore.getInstance().collection("user").document(currentUser.uid)
                    detailsRef.get().addOnSuccessListener { document ->
                        if (document != null && document.exists() && document.contains("height")) {
                            startActivity(Intent(requireActivity(), Home_screen::class.java))
                            requireActivity().finish()
                        } else {
                            // Missing height, show height picker
                            weightv.visibility = View.GONE
                            height.visibility = View.VISIBLE
                        }
                    }.addOnFailureListener {
                        // Fallback: navigate home if Firestore fails
                        startActivity(Intent(requireActivity(), Home_screen::class.java))
                        requireActivity().finish()
                    }
                } else {
                    // Offline: Save weight locally and navigate home
                    addweight()
                    Toast.makeText(requireContext(), "Saved locally (Offline)", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireActivity(), Home_screen::class.java))
                    requireActivity().finish()
                }
            }
            
            addh.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (isInternetOn(requireContext()) && currentUser != null && !currentUser.isAnonymous) {
                    val hValue = ft.value + (inch.value.toFloat() / 12f)
                    FirebaseFirestore.getInstance().collection("user").document(currentUser.uid)
                        .update("height", hValue.toString()).addOnSuccessListener {
                            startActivity(Intent(requireActivity(), Home_screen::class.java))
                            requireActivity().finish()
                        }.addOnFailureListener {
                            startActivity(Intent(requireActivity(), Home_screen::class.java))
                            requireActivity().finish()
                        }
                } else {
                    // Guest or Offline: Go home
                    startActivity(Intent(requireActivity(), Home_screen::class.java))
                    requireActivity().finish()
                }
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addweight() {
        val weight = binding.weight.value.toString()
        val curr_date = LocalDate.now().toString()
        Constant.savedata(requireContext(), "weight", "curr_w", weight.toString())
        val map = hashMapOf("weight" to weight, "date" to curr_date)
        userDitails.collection("Weight track").document(curr_date).set(map)
    }
}
