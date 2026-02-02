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
                showHeightDialog()
            }
            layAge.setOnClickListener {
                showEditDialog("Age", 1, 120, binding.age.text.toString().toIntOrNull() ?: 20) { newValue ->
                    updateProfileField("dob", "01/01/${Calendar.getInstance().get(Calendar.YEAR) - newValue}")
                }
            }
            layWeight.setOnClickListener {
                showEditDialog("Weight (kg)", 20, 250, binding.edweight.text.toString().toIntOrNull() ?: 70) { newValue ->
                    Constant.savedata(requireActivity(), "weight", "curr_w", newValue.toString())
                    binding.edweight.text = newValue.toString()
                    UserDetails() // Recalculate BMI
                }
            }
            val currentUser = FirebaseAuth.getInstance().currentUser
            logout.setOnClickListener {
                handleLogout(currentUser)
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
                    val address = addresses!![0]
                    val land=address.subLocality
                    val state = address.adminArea
                    val country = address.countryName
//                    val district = address.subAdminArea
                    val locationName = address.locality
                    val FullAddress = "$land,$locationName,$state,$country"
                    binding.locat.text=FullAddress
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
        ft.value = floor(height).toInt()
        ft.wrapSelectorWheel = true
        inch.minValue = 1
        inch.maxValue = 12
        inch.value = round((height - floor(height)) * 12).toInt()
        inch.wrapSelectorWheel = true
        add.setOnClickListener {
            var heightVal = ft.value.toDouble() + (inch.value.toDouble() / 12)
            heightVal = df.format(heightVal).toDouble()
            viewModel.updateHeight(heightVal.toString(), requireContext())
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
        FirebaseFirestore.getInstance().collection("user").document(uid)
            .update(field, value)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                UserDetails()
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
            binding.logout.visibility = View.GONE // Hide logout for Guests
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
            userDitails?.addSnapshotListener { it, error ->
                if (error != null) return@addSnapshotListener
                if (it != null && it.exists()) {
                    binding.username.text = it.data?.get("fullname").toString()
                    binding.email.text = it.data?.get("email").toString()
                    binding.tvLet.text = binding.username.text.toString()[0].toString()
                    val dob = it.data?.get("dob").toString()
                    if (dob.length >= 4) {
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
                    binding.Gender.text = it.data?.get("gender").toString()
                }
            }
        }
        viewModel.getHeight()
        viewModel.data.observe(requireActivity()){ state->
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
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val reference = FirebaseFirestore.getInstance().collection("user").document(currentUserUid)
            reference.addSnapshotListener { it, e ->
                if (e != null) return@addSnapshotListener
                if (it != null && it.exists()) {
                    var heightf = it.data!!["height"]?.toString()?.toDouble() ?: 0.0
                    height = heightf
                    heightf *= 0.305
                    var Bmi: String = "0"
                    val weight = Constant.loadData(requireActivity(), "weight", "curr_w", "0").toString().toDouble()
                    if (weight == 0.0 || heightf == 0.0) {
                        Bmi = "0"
                    } else {
                        Bmi = Math.round(((weight / (heightf * heightf))).toDouble())
                            .toString()
                    }
                    binding.bmi.text = Bmi
                    var bm = Bmi.toDouble()
                    if (bm < 18.5) {
                        binding.measure.text = "You are underweight"
                        binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
                        binding.measure.setTextColor(Color.parseColor("#FFD600")) // Warning yellow
                    } else if (bm < 29.9 && bm > 25.0) {
                        binding.measure.text = "You are Overweight"
                        binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
                        binding.measure.setTextColor(Color.parseColor("#FF3D00")) // Error orange
                    } else if (bm > 30.0) {
                        binding.measure.text = "You are Obese Range"
                        binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
                        binding.measure.setTextColor(Color.parseColor("#FF3D00"))
                    } else {
                        binding.measure.text = "You are Normal and Healthy"
                        binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
                        binding.measure.setTextColor(Color.parseColor("#00E676")) // Success green
                    }
                } else {
                    // Guest or no data
                    binding.bmi.text = "0.0"
                    binding.measure.text = "Enter details to see BMI"
                    binding.measure.setBackgroundResource(R.drawable.glass_rounded_12)
                    height = 0.0
                }
            }
        }
        
        binding.layGender.setOnClickListener {
            showGenderDialog()
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
}
