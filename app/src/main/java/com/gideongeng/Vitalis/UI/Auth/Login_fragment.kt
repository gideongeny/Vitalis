package com.gideongeng.Vitalis.UI.Auth

//import com.github.ybq.android.spinkit.SpinKitView
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.gideongeng.Vitalis.R
import com.gideongeng.Vitalis.UI.Home.Home_screen
import com.google.android.material.textfield.TextInputLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Login_fragment():Fragment() {
    var root: View? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private var verificationId: String? = null
    private val RC_SIGN_IN = 9001
    @SuppressLint("UseRequireInsteadOfGet", "SetTextI18n")
    private fun createuserlog()
    {
        val memail: EditText = root!!.findViewById(R.id.email)
        val mpassword: EditText =root!!.findViewById(R.id.password)
//        val progressBar: SpinKitView =root!!.findViewById(R.id.progress_bar)
        val pass_error:TextView=root!!.findViewById(R.id.password_error)
        val rightAnimation: Animation? = AnimationUtils.loadAnimation(activity, R.anim.rightt_left)
        val leftAnimation: Animation? = AnimationUtils.loadAnimation(activity, R.anim.left_right)
        val layemail:TextInputLayout=root!!.findViewById(R.id.tvemail)
        val laypass:TextInputLayout=root!!.findViewById(R.id.tvpass)
        pass_error.startAnimation(leftAnimation!!)
        layemail.startAnimation(rightAnimation!!)
        laypass.startAnimation(leftAnimation!!)

        val  fAuth: FirebaseAuth = FirebaseAuth.getInstance()
        try {
            if (memail.text.toString().isEmpty()) {
                memail.error = "Email is Required."
                return
            }
            if (mpassword.text.toString().isEmpty()) {
                pass_error.text="Password is Required *"
                return
            }
            if (mpassword.text.toString().length < 6) {
                pass_error.text="Password Must be greater than 6 Characters *"
                return
            }
//            progressBar.visibility = View.VISIBLE

            fAuth.signInWithEmailAndPassword(memail.text.toString(),mpassword.text.toString()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = fAuth.currentUser
                    user?.reload()?.addOnCompleteListener {
                        if(user.isEmailVerified){
                            Toast.makeText(activity, "Logged in Successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(activity, "Logged in. Please verify your email later.", Toast.LENGTH_LONG).show()
                        }
                        startActivity(Intent(activity, Home_screen::class.java))
                        activity!!.finish()
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Error ! " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
//                    progressBar.visibility(View.GONE)
                }
            }
        }
        catch (e:Exception)
        {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }
    fun forget(){
        val  fAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val forgotTextLink: TextView = root!!.findViewById(R.id.forgotPassword)
        forgotTextLink.setOnClickListener(View.OnClickListener { v ->
            val resetMail = EditText(v.context)
            val passwordResetDialog = AlertDialog.Builder(v.context)
            passwordResetDialog.setTitle("Reset Password ?")
            passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.")
            passwordResetDialog.setView(resetMail)
            passwordResetDialog.setPositiveButton(
                "Yes"
            ) { dialog, which -> // extract the email and send reset link
                val mail = resetMail.text.toString()
                if (resetMail.text.toString().isEmpty()) {
                    resetMail.error = "Email is Required."
                    Toast.makeText(activity,"Error: Email is Required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                else{
                    fAuth.sendPasswordResetEmail(mail).addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            "Reset Link Sent To Your Email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            activity,
                            "Error ! Reset Link is Not Sent" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            passwordResetDialog.setNegativeButton(
                "No"
            ) { dialog, which ->
                // close the dialog
            }
            passwordResetDialog.create().show()
        })
    }
    fun userlog(){
        var btnEnable=true
        val logButton: Button =root!!.findViewById(R.id.loginbtn)
        logButton.setOnClickListener(View.OnClickListener {
            if(btnEnable)
            {
                btnEnable=false
                createuserlog()
            }
            Handler().postDelayed({
                btnEnable=true
            },1000)
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root=inflater.inflate(R.layout.activity_login_fragment,container,false)
        userlog()
        forget()
        setupSocialButtons()
        setupGoogleSignIn()
        return root
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.vitalis_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupSocialButtons() {
        root!!.findViewById<View>(R.id.googleBtn).setOnClickListener {
            signInWithGoogle()
        }
        root!!.findViewById<View>(R.id.phoneBtn).setOnClickListener {
            showPhoneAuthDialog()
        }
        root!!.findViewById<View>(R.id.anonymousBtn).setOnClickListener {
            signInAnonymously()
        }
    }

    private fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onAuthSuccess()
            } else {
                Toast.makeText(activity, "Anonymous Sign-In Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        // Diagnostic: Verify the Web Client ID being used
        val clientId = getString(R.string.vitalis_web_client_id)
        android.util.Log.d("VITALIS_AUTH", "Starting Google Sign-In with Web Client ID: $clientId")
        
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                val statusCode = e.statusCode
                val message = "Google Sign-In Failed (Code $statusCode): ${e.message}"
                android.util.Log.e("VITALIS_AUTH", message, e)
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                
                if (statusCode == 10) {
                    Toast.makeText(activity, "Error 10: Usually means SHA-1 MISMATCH or Support Email missing in Firebase.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onAuthSuccess()
            } else {
                Toast.makeText(activity, "Firebase Google Auth Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhoneAuthDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_phone_auth, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(dialogView).create()
        
        val phoneInput = dialogView.findViewById<EditText>(R.id.phoneNumber)
        val otpLayout = dialogView.findViewById<View>(R.id.otpLayout)
        val otpInput = dialogView.findViewById<EditText>(R.id.otpCode)
        val btn = dialogView.findViewById<Button>(R.id.actionButton)

        btn.setOnClickListener {
            if (otpLayout.visibility == View.GONE) {
                val number = phoneInput.text.toString()
                if (number.isNotEmpty()) {
                    sendVerificationCode(number, dialogView, dialog)
                }
            } else {
                val code = otpInput.text.toString()
                if (code.isNotEmpty()) {
                    verifyCode(code)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun sendVerificationCode(number: String, view: View, dialog: AlertDialog) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(credential)
                    dialog.dismiss()
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    Toast.makeText(activity, "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    view.findViewById<View>(R.id.otpLayout).visibility = View.VISIBLE
                    view.findViewById<Button>(R.id.actionButton).text = "Verify OTP"
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onAuthSuccess()
            } else {
                Toast.makeText(activity, "Phone Sign-In Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onAuthSuccess() {
        Toast.makeText(activity, "Logged in Successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(activity, Home_screen::class.java))
        activity!!.finish()
    }

}
