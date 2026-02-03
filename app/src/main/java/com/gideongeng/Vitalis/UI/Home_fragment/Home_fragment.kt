package com.gideongeng.Vitalis.UI.Home_fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.gideongeng.Vitalis.R
import com.gideongeng.Vitalis.UI.Food.Add_food
import com.gideongeng.Vitalis.UI.Food.Food_track
import com.gideongeng.Vitalis.UI.Food.ViewModel.Food_ViewModel
import com.gideongeng.Vitalis.UI.Reminder.MealReminder
import com.gideongeng.Vitalis.UI.weight.weight_track
import com.gideongeng.Vitalis.Utils.Constant
import com.gideongeng.Vitalis.databinding.FragmentHomeFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.*
import kotlin.math.abs
import kotlin.math.round

@AndroidEntryPoint
@Suppress("SENSELESS_COMPARISON")
@RequiresApi(Build.VERSION_CODES.O)
class Home_fragment : Fragment() {
    private var sensorManager: SensorManager? = null
    private var no_glass: Int? = null
    private val handler = Handler()
    private lateinit var dialog: Dialog
    private lateinit var binding: FragmentHomeFragmentBinding
    var userDitails: DocumentReference = FirebaseFirestore.getInstance().collection("user").document(
        FirebaseAuth.getInstance().currentUser!!.uid.toString()
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private var curr_date: String = LocalDate.now().toString()
    private val updatetimeRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SimpleDateFormat")
        override fun run() {
            stepCounter()
            greeting_class()
            if (!Constant.isInternetOn(requireContext())) {
                binding.net.visibility = View.VISIBLE
            } else {
                binding.net.visibility = View.GONE
            }
            handler.postDelayed(this, 1000)
        }

    }

    @SuppressLint("SuspiciousIndentation", "CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeFragmentBinding.inflate(inflater, container, false)
        handler.post(updatetimeRunnable)
//        try {
            dialog = Dialog(requireActivity())
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                binding.name.text = currentUser.displayName ?: "User"
                binding.pInitial.text = binding.name.text[0].uppercase().toString()
                
                currentUser.photoUrl?.let {
                    Glide.with(this).load(it).into(binding.pImg)
                    binding.pImg.visibility = View.VISIBLE
                    binding.pInitial.visibility = View.GONE
                }
            }

            userDitails.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    val fullname = value.data?.get("fullname")?.toString()
                    if (!fullname.isNullOrEmpty() && fullname != "null") {
                        binding.name.text = fullname
                    }
                }
            }
        set_target()
            existwater()
            addwater()
            addfood()
            addTarget()
            binding.apply {
                profileCard.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, profile_fragment())
                        .addToBackStack(null)
                        .commit()
                }

                weightButton.setOnClickListener {
                    if (requiresAuthentication("track your weight")) return@setOnClickListener
                    startActivity(Intent(requireActivity(), weight_track::class.java))
                }
                mealrem.setOnClickListener {
                    if (requiresAuthentication("manage meal reminders")) return@setOnClickListener
                    startActivity(Intent(requireActivity(), MealReminder::class.java))
                }
                circularProgressBar.setOnClickListener {
                    Toast.makeText(activity, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
                }
                circularProgressBar.setOnLongClickListener {
                    if (requiresAuthentication("reset steps")) return@setOnLongClickListener true
                    reset()
                    true
                }
                calorieT.setOnClickListener {
                    if (requiresAuthentication("track your calories")) return@setOnClickListener
                    startActivity(Intent(requireActivity(),Food_track::class.java))
                }
            }

//            var cpbar = binding.circularProgressBar
////            reset step counter
//            try {
//                cpbar.setOnClickListener {
//                    Toast.makeText(activity, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
//                }
//                cpbar.setOnLongClickListener {
//                    reset()
//                    true
//                }
//
//            } catch (e: Exception) {
//                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
//            }
            ////////////////////////////////////////////


//        } catch (e: Exception) {
//            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
//        }
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return binding.root
    }

    ///////////////////////////////////////////////

    override fun onStart() {
        super.onStart()
        getenergy()
        Getlatestweight()
    }

    // greeting user
    private fun greeting_class() {
        val time: ImageView = binding.weather
        val greeting: TextView = binding.greeting
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour.toInt() in 6..17) {
            time.setImageResource(R.drawable.sun)
            if (hour.toInt() in 6..12) {
                greeting.text = "Good Morning !"
            }
            if (hour.toInt() in 13..14) {
                greeting.text = "Good Noon !"
            } else if (hour.toInt() in 15..17) {
                greeting.text = "Good Afternoon !"
            }
        } else {
            time.setImageResource(R.drawable.moon)
            if (hour.toInt() in 18..19) {
                greeting.text = "Good Evening !"
            } else {
                greeting.text = "Good Night !"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updatetimeRunnable)
    }

    //    control daily water intake
    private fun addwater() {
        userDitails.collection("water track").document(LocalDate.now().toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val glass: String = snapshot.data?.get("glass").toString()
                    try {
                        if (glass == "" || glass == null || glass.isEmpty()) {
                            no_glass = 0
                        } else no_glass = glass.toInt()
                    } catch (_: Exception) {
                        no_glass = 0
                    }
                } else {
                    Log.d("TAG", "Current data null")
                }
            }

        binding.addwater.setOnClickListener {
            if (requiresAuthentication("track your water intake")) return@setOnClickListener
            binding.deletewater.isClickable = true
            binding.deletewater.setBackgroundResource(R.drawable.baseline_remove_circle_outline_24)
            no_glass = no_glass?.plus(1)
            val curr_date = LocalDate.now()
            val water = mapOf(
                "Date" to curr_date.toString(),
                "glass" to no_glass.toString()
            )
            userDitails.collection("water track").document(curr_date.toString()).set(water)
            if (no_glass == 10) {
                val cong: LottieAnimationView = binding.animationView
                cong.visibility = View.VISIBLE
                binding.animationView2.visibility = View.VISIBLE
                cong.playAnimation()
                binding.animationView2.playAnimation()
                Handler().postDelayed({
                    cong.visibility = View.GONE
                    cong.cancelAnimation()
                    binding.animationView2.visibility = View.GONE
                    binding.animationView2.cancelAnimation()
                }, 2000)
            }
            updatewater()
        }
        val reduceWater: ImageButton = binding.deletewater
        reduceWater.setOnClickListener {
            if (requiresAuthentication("track your water intake")) return@setOnClickListener
            if (binding.waterLevel.text.toString() == "0") {
                binding.deletewater.isClickable = false
                binding.deletewater.setBackgroundResource(R.drawable.disable_remove)
            } else {
                no_glass = no_glass?.minus(1)
                val curr_date = LocalDate.now()
                val water = mapOf(
                    "Date" to curr_date.toString(),
                    "glass" to no_glass.toString()
                )
                userDitails.collection("water track").document(curr_date.toString()).set(water)
                updatewater()
            }
        }
    }

    // reset the steps
    private fun reset() {
        val pre_step =
            Constant.loadData(requireContext(), "step_count", "total_step", "0")!!.toInt()
        Constant.savedata(requireContext(), "step_count", "previous_step", pre_step.toString())
        stepCounter()
        dataupload("0", LocalDate.now().toString())
    }

    //    upload steps data in firestore
    private fun dataupload(currsteps: String, curr_date: String) {
        val steps = hashMapOf(
            "steps" to currsteps.toString(),
            "date" to curr_date.toString()
        )
        val curruser = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("user").document(curruser.toString())
            .collection("steps").document(curr_date.toString()).set(steps)
    }

    private fun existwater() {
        val curr_date = LocalDate.now()
        val water = mapOf(
            "Date" to curr_date.toString(),
            "glass" to "0"
        )
        userDitails.collection("water track").document(curr_date.toString()).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    userDitails.collection("water track").document(curr_date.toString()).set(water)
                    updatewater()
                } else {
                    updatewater()
                }
            }
    }

    //    update water level at UI textview
    private fun updatewater() {
        userDitails.collection("water track").document(curr_date.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val gls = snapshot.data?.get("glass").toString()
                    val water_level: TextView = binding.waterLevel
                    water_level.text = gls.toString()
                    val wt: ConstraintLayout = binding.content
                }
            }
    }

    //    add your daily meal
    private fun addfood() {
        binding.addFood.setOnClickListener {
            if (requiresAuthentication("log your food")) return@setOnClickListener
            val intent = Intent(activity, Add_food::class.java)
            startActivity(intent)
        }
    }

    fun set_target() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val target = sharedPreferences.getString("target", "1000")
        val cpbar = binding.circularProgressBar
        cpbar.progressMax = target!!.toFloat()
        binding.goal.text = target.toString()
    }

    fun stepCounter() {
        val t_step = Constant.loadData(requireContext(), "step_count", "total_step", "0").toString()
        val pre_step =
            Constant.loadData(requireContext(), "step_count", "previous_step", "0").toString()
        val curr_step = abs(t_step.toInt() - pre_step.toInt()).toString()
        binding.walk.text = curr_step.toString()
        binding.burn.text = (round(curr_step.toFloat() * 0.04).toInt()).toString()
        val cpbar = binding.circularProgressBar
        cpbar.progress = curr_step.toFloat()
        val burnprobar = binding.burnCal
        burnprobar.progress = ((round(curr_step.toFloat() * 0.04).toInt()).toFloat())
        burnprobar.setOnClickListener {
            Toast.makeText(requireContext(), "Long press to set target", Toast.LENGTH_SHORT).show()
        }
    }

    fun addTarget() {
        dialog.setContentView(R.layout.pop_weight)
        val burn_target: NumberPicker = dialog.findViewById(R.id.loss)
        val add: AppCompatButton = dialog.findViewById(R.id.add)
        val save_burn = Constant.loadData(requireContext(), "calorie", "burn", "100").toString()
        burn_target.minValue = 0
        burn_target.maxValue = 14
        burn_target.wrapSelectorWheel = true
        burn_target.displayedValues = Constant.calorieburn
        val burn_cal = binding.burnCal
        burn_cal.progressMax = save_burn.toFloat()
        burn_cal.setOnLongClickListener {
            if (requiresAuthentication("set calorie targets")) return@setOnLongClickListener true
            dialog.show()
            val save = Constant.loadData(requireContext(), "calorie", "burn", "100").toString()
            burn_target.value = Constant.calorieburn.indexOf(save)
            true
        }
        add.setOnClickListener {
            Constant.savedata(
                requireContext(),
                "calorie",
                "burn",
                Constant.calorieburn[burn_target.value]
            )
            burn_cal.progressMax = Constant.calorieburn[burn_target.value].toFloat()
            dialog.dismiss()
        }
    }

    fun getenergy() {
        val cal = binding.calorie
        val cal_meter = binding.calMeter
        val ViewModel = ViewModelProvider(this)[Food_ViewModel::class.java]
        val target=Constant.loadData(requireContext(), "calorie", "target", "100").toString().toFloat()
        cal_meter.progressMax=target
        binding.targetCal.text=target.toString()
        ViewModel.calories.observe(viewLifecycleOwner) { nutrients ->
            val total = nutrients.sum()
            if (total > target) {
                cal_meter.progressBarColor = Color.RED
            } else {
                cal_meter.progressBarColor = Color.YELLOW
            }
            cal.text = total.toString()
            cal_meter.progress = total.toFloat()
        }

        ViewModel.getCalories()

    }

    fun Getlatestweight() {
        binding.weight.text =
            Constant.loadData(requireContext(), "weight", "curr_w", "").toString()
        // binding.target.text = Constant.loadData(requireContext(), "weight", "loss", "0").toString()
    }

    private fun requiresAuthentication(action: String): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Sign In Required")
                .setMessage("To $action and save your progress, please sign in with an account.")
                .setPositiveButton("Sign In") { _, _ ->
                    startActivity(Intent(activity, MainAuthentication::class.java))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return true
        }
        return false
    }
}

