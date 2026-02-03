package com.gideongeng.Vitalis.UI.Home_fragment.Profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gideongeng.Vitalis.R
import com.gideongeng.Vitalis.UI.Auth.MainAuthentication
import com.gideongeng.Vitalis.UI.Home.Home_screen
import com.gideongeng.Vitalis.UI.step.StepsTrack
import com.gideongeng.Vitalis.UI.water.Water
import com.gideongeng.Vitalis.Utils.Constant
import com.gideongeng.Vitalis.Utils.UIstate
import com.gideongeng.Vitalis.databinding.FragmentProfileFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.bumptech.glide.Glide
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.floor
import kotlin.math.round
import kotlin.properties.Delegates

class profile_fragment : Fragment() {
    private val userDitails: DocumentReference? by lazy {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("user").document(uid)
        }
    }
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding:FragmentProfileFragmentBinding
    private lateinit var dialog: Dialog
    private  lateinit var viewModel:Profile_ViewModel
    private val df = DecimalFormat("#.##")
    private var height by Delegates.notNull<Double>()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentProfileFragmentBinding.inflate(inflater,container,false)
        viewModel=ViewModelProvider(requireActivity())[Profile_ViewModel::class.java]
        binding.edweight.text = Constant.loadData(requireActivity(), "weight", "curr_w", "0").toString()
        getlocation ()
        return binding.root
    }
    private fun updateBmiStatus(bm: Double) {
        if (bm < 18.5) {
            binding.measure.text = "You are underweight"
            binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
            binding.measure.setTextColor(Color.parseColor("#FFD600"))
        } else if (bm < 29.9 && bm > 25.0) {
            binding.measure.text = "You are Overweight"
            binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
            binding.measure.setTextColor(Color.parseColor("#FF3D00"))
        } else if (bm > 30.0) {
            binding.measure.text = "You are Obese Range"
            binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
            binding.measure.setTextColor(Color.parseColor("#FF3D00"))
        } else {
            binding.measure.text = "You are Normal and Healthy"
            binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
            binding.measure.setTextColor(Color.parseColor("#00E676"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog= Dialog(requireContext())
        UserDetails()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            startActivity(Intent(requireActivity(), Home_screen::class.java))
            requireActivity().finish()
        }

        binding.apply {
            steps.setOnClickListener {
                startActivity(Intent(requireActivity(), StepsTrack::class.java))
            }
            waterT.setOnClickListener {
                startActivity(Intent(activity, Water::class.java))
            }
            layHeight.setOnClickListener {
                if (requiresAuthentication("edit your height")) return@setOnClickListener
                showHeightDialog()
            }
            layAge.setOnClickListener {
                if (requiresAuthentication("edit your age")) return@setOnClickListener
                showEditDialog("Age", 1, 120, binding.age.text.toString().toIntOrNull() ?: 20) { newValue ->
                    updateProfileField("dob", "01/01/${Calendar.getInstance().get(Calendar.YEAR) - newValue}")
                }
            }
            layWeight.setOnClickListener {
                if (requiresAuthentication("edit your weight")) return@setOnClickListener
                showEditDialog("Weight (kg)", 20, 250, binding.edweight.text.toString().toIntOrNull() ?: 70) { newValue ->
                    Constant.savedata(requireActivity(), "weight", "curr_w", newValue.toString())
                    binding.edweight.text = newValue.toString()
                    updateProfileField("weight", newValue.toString())
                }
            }
            
            val currentUser = FirebaseAuth.getInstance().currentUser
            logout.setOnClickListener {
                handleLogout(currentUser)
            }

            layGender.setOnClickListener {
                if (requiresAuthentication("edit your gender")) return@setOnClickListener
                showGenderDialog()
            }

            measure.setOnClickListener {
                if (requiresAuthentication("update your stats")) return@setOnClickListener
                showHeightDialog()
            }
        }

        viewModel.getHeight()
        viewModel.data.observe(viewLifecycleOwner){ state->
            when (state){
                is UIstate.Loading -> {
                    binding.mainContent.visibility=View.GONE
                    binding.progressBar.visibility=View.VISIBLE
                }
                is UIstate.Failure -> {
                    Toast.makeText(requireContext(), state.error.toString(), Toast.LENGTH_SHORT).show()
                }
                is UIstate.Success -> {
                    binding.mainContent.visibility=View.VISIBLE
                    binding.progressBar.visibility=View.GONE
                    binding.edhight.text=state.data.toString()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    fun getlocation ()
    {
        val locationManagerr = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(locationManagerr.isLocationEnabled)
        {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    currentLocation: Location?->
                if (currentLocation!=null && Constant.isInternetOn(requireActivity()))
                {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val land = address.subLocality ?: ""
                        val state = address.adminArea ?: ""
                        val country = address.countryName ?: ""
                        val locationName = address.locality ?: ""
                        val FullAddress = "$land,$locationName,$state,$country"
                        binding.locat.text = FullAddress
                    }
                }
            }.addOnFailureListener {
                    exception->
                Toast.makeText(requireContext(), "Please Enable Your Location", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(requireContext(), "Please Enable Your Location", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showHeightDialog() {
        dialog.setContentView(R.layout.pop_height)
        val ft: NumberPicker = dialog.findViewById(R.id.ft)
        val inch: NumberPicker = dialog.findViewById(R.id.inch)
        val add: AppCompatButton = dialog.findViewById(R.id.add)
        ft.minValue = 3
        ft.maxValue = 8
        ft.value = 5 // Default
        ft.wrapSelectorWheel = true
        inch.minValue = 1
        inch.maxValue = 12
        inch.value = 6 // Default
        inch.wrapSelectorWheel = true
        add.setOnClickListener {
            var heightVal = ft.value.toDouble() + (inch.value.toDouble() / 12)
            heightVal = df.format(heightVal).toDouble()
            viewModel.updateHeight(heightVal.toString(), requireContext())
            updateProfileField("height", heightVal.toString())
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showEditDialog(title: String, min: Int, max: Int, current: Int, onSave: (Int) -> Unit) {
        val editDialog = Dialog(requireContext())
        editDialog.setContentView(R.layout.pop_edit_profile)
        val titleTv: TextView = editDialog.findViewById(R.id.popTitle)
        val picker: NumberPicker = editDialog.findViewById(R.id.editPicker)
        val saveBtn: AppCompatButton = editDialog.findViewById(R.id.saveBtn)

        titleTv.text = "Update $title"
        picker.minValue = min
        picker.maxValue = max
        picker.value = current

        saveBtn.setOnClickListener {
            onSave(picker.value)
            editDialog.dismiss()
        }
        editDialog.show()
    }

    private fun updateProfileField(field: String, value: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = hashMapOf(field to value)
        FirebaseFirestore.getInstance().collection("user").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    UserDetails()
                }
            }
            .addOnFailureListener { e: Exception ->
                if (isAdded) {
                    Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleLogout(user: FirebaseUser?) {
        activity?.let { act ->
            if (user != null && !user.isAnonymous) {
                FirebaseAuth.getInstance().signOut()
            }
            val intent = Intent(act, MainAuthentication::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            act.startActivity(intent)
            act.finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun UserDetails()
    {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            binding.username.text = "Guest User"
            binding.email.text = "Sign in to save your progress"
            binding.tvLet.text = "G"
            binding.age.text = "N/A"
            binding.Gender.text = "N/A"
            binding.loginCard.visibility = View.VISIBLE
            binding.logout.visibility = View.GONE
            binding.loginBtn.setOnClickListener {
                startActivity(Intent(activity, MainAuthentication::class.java))
                requireActivity().finish()
            }
        } else {
            binding.loginCard.visibility = View.GONE
            binding.logout.visibility = View.VISIBLE
            binding.logout.text = "Logout"
            binding.logout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(activity, MainAuthentication::class.java))
                requireActivity().finish()
            }

            binding.username.text = currentUser.displayName ?: "User"
            binding.email.text = currentUser.email ?: ""
            if (binding.username.text.isNotEmpty()) {
                binding.tvLet.text = binding.username.text[0].uppercase().toString()
            }
            
            currentUser.photoUrl?.let {
                Glide.with(this).load(it).into(binding.profileImg)
                binding.profileImg.visibility = View.VISIBLE
                binding.tvLet.visibility = View.GONE
            }

            userDitails?.addSnapshotListener { it, error ->
                if (error != null) return@addSnapshotListener
                if (it != null && it.exists()) {
                    val fullname = it.data?.get("fullname")?.toString()
                    val email = it.data?.get("email")?.toString()
                    val genderVal = it.data?.get("gender")?.toString() ?: "N/A"
                    val heightVal = it.data?.get("height")?.toString() ?: "0"
                    
                    if (!fullname.isNullOrEmpty() && fullname != "null") {
                        binding.username.text = fullname
                        binding.tvLet.text = fullname[0].uppercase().toString()
                    }
                    if (!email.isNullOrEmpty() && email != "null") {
                        binding.email.text = email
                    }
                    
                    binding.Gender.text = genderVal
                    binding.edhight.text = heightVal

                    val dob = it.data?.get("dob").toString()
                    if (dob.length >= 4 && dob != "null") {
                        try {
                            val birthyear = dob.substring(dob.length - 4).toInt()
                            var currentYear = LocalDate.now().year
                            binding.age.text = (currentYear - birthyear).toString()
                        } catch (e: Exception) {
                            binding.age.text = "N/A"
                        }
                    } else {
                        binding.age.text = "N/A"
                    }

                    val weightVal = Constant.loadData(requireActivity(), "weight", "curr_w", "0").toString().toDouble()
                    val heightNum = heightVal.toDoubleOrNull() ?: 0.0
                    val heightMeters = heightNum * 0.305
                    
                    if (weightVal > 0 && heightMeters > 0) {
                        val bmiVal = Math.round(weightVal / (heightMeters * heightMeters)).toString()
                        binding.bmi.text = bmiVal
                        updateBmiStatus(bmiVal.toDouble())
                    } else {
                        binding.bmi.text = "0.0"
                        binding.measure.text = "Enter details for BMI"
                    }
                }
            }
        }
    }

    private fun showGenderDialog() {
        val genders = arrayOf("Male", "Female", "Other")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Gender")
        builder.setItems(genders) { dialog, which ->
            updateProfileField("gender", genders[which])
            dialog.dismiss()
        }
        builder.show()
    }

    private fun requiresAuthentication(action: String): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Sign In Required")
                .setMessage("To $action and save your progress, please sign in with an account.")
                .setPositiveButton("Sign In") { _, _ ->
                    startActivity(Intent(activity, MainAuthentication::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
            return true
        }
        return false
    }
}
