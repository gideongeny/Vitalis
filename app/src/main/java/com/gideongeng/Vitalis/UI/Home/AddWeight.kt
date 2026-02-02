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
                    
                    // Check if height exists
                    userDitails.get().addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            if (document.contains("height")) {
                                // Height already exists, go home
                                startActivity(Intent(requireActivity(), Home_screen::class.java))
                                requireActivity().finish()
                            } else {
                                // Height missing, show height picker
                                weightv.visibility = View.GONE
                                height.visibility = View.VISIBLE
                                addh.setOnClickListener {
                                    if (isInternetOn(requireContext())) {
                                        val hValue = ft.value + (inch.value.toFloat() / 12f)
                                        userDitails.update("height", hValue.toString()).addOnSuccessListener {
                                            startActivity(Intent(requireActivity(), Home_screen::class.java))
                                            requireActivity().finish()
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "Internet connection required", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Internet connection required", Toast.LENGTH_SHORT).show()
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
