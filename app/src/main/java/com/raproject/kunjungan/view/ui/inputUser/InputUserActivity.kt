package com.raproject.kunjungan.view.ui.inputUser

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raproject.kunjungan.R
import com.raproject.kunjungan.databinding.ActivityInputUserBinding
import com.raproject.kunjungan.view.ui.home.HomeActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class InputUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputUserBinding
    private lateinit var viewModel: InputUserViewModel
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mResendToken: ForceResendingToken
    private lateinit var mVerificationId: String
    private var photoPath: String? = null
    lateinit var filePath: Uri
    private var imageUrl: String = ""

    //firebase storage
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_input_user)
        this.viewModel = ViewModelProvider(this).get(InputUserViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.btnSave.setOnClickListener {
            if (photoPath != null){

                val ref = storageReference.child("images/" + UUID.randomUUID().toString())
                ref.putFile(filePath)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            imageUrl = it.toString()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "upload foto gagal", Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener {

                    }
            }
            viewModel.insertData(
                binding.edtNik.text.toString(),
                imageUrl,
                binding.edtNama.text.toString(),
                binding.edtTanggalLahir.text.toString(),
                binding.edtAlamat.text.toString(),
                "+62"+binding.edtNoHp.text.toString(),
                "1",
                "aktif",
                binding.edtGaji.text.toString().toInt(),
                binding.spBank.selectedItem.toString(),
                binding.edtSalesRespon.text.toString()
            )

            viewModel.response.observe(this, Observer {
                if(it == "1"){
                    progress_circular.visibility = View.VISIBLE
                }else if (it == "2"){
                    progress_circular.visibility = View.GONE
                    finish()
                    startActivity(Intent(this, HomeActivity::class.java))
                }
            })
        }

        viewModel.response.observe(this, Observer {
            if (it == "2"){
                finish()
            }
        })

        mAuth = FirebaseAuth.getInstance()
        binding.btnOtp.setOnClickListener{
            requestOtp("+62"+binding.edtNoHp.text.toString())
            binding.btnValidasiOtp.visibility = View.VISIBLE
        }

        binding.btnValidasiOtp.setOnClickListener{
            val credential = PhoneAuthProvider.getCredential(mVerificationId, binding.edtOtp.text.toString())
            signInWithPhoneAuthCredential(credential)

        }

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        binding.ivAdd.setOnClickListener{
            if (checkAndRequestPermissions()){
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    val photoFile: File
                    try {
                        val storageDir = filesDir
                        photoFile = File.createTempFile(
                            "SNAPSHOT",
                            ".jpg",
                            storageDir
                        )
                        photoPath = photoFile.absolutePath
                    }catch (ex: IOException){
                        return@setOnClickListener
                    }

                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "com.raproject.kunjungan.fileprovider",
                        photoFile
                    )

                    filePath = photoURI

                    val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intentCamera, REQUEST_TAKE_PHOTO)

                }
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean{
        val listPermissionsNeeded = ArrayList<String>()
        val writeExternalCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val accessNetworkState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
        val internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)

        if (writeExternalCamera != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if( accessNetworkState != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if (internet != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.INTERNET)
        }
        if (listPermissionsNeeded.isNotEmpty()){
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), PERMISSION_CALLBACK_CONSTANT)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSION_CALLBACK_CONSTANT -> {
                var allGranted = false
                for (i in grantResults.indices){
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                        allGranted = true
                    }else{
                        allGranted = false
                        break
                    }
                }

                if (grantResults.isNotEmpty() && !allGranted){
                    Toast.makeText(this, "This app needs permissions.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (photoPath != null) {

                val photo = data!!.extras!!["data"] as Bitmap?
                binding.ivProfile.setImageBitmap(photo)
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("otp", "onVerificationCompleted:$credential")
            binding.btnValidasiOtp.visibility = View.GONE
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.d("otp", "onVerificationFailed: $e")

            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("debug",e.toString());

            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("otp", e.toString());

            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d("otp", "onCodeSent:$verificationId")

            binding.btnOtp.visibility = View.GONE
            mVerificationId = verificationId
            mResendToken = token
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verifikasi no telepon berhasil", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"Kode OTP salah",Toast.LENGTH_SHORT).show();
                }
            }
    }

    private fun requestOtp(phoneNumber: String){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, 120, TimeUnit.SECONDS, this, callbacks)
    }

    private fun sendOtpCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(mVerificationId, code)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("otp", "signInWithCredential:success")

                    FirebaseAuth.getInstance().signOut()
                } else {
                    Log.d("otp", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.e("err", task.exception.toString())
                        Toast.makeText(this, "Kode Verifikasi Salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun resendCodeVerify(phone: String?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone!!, 120, TimeUnit.SECONDS, this, callbacks, mResendToken
        )
        Toast.makeText(this, "Kode dikirim ulang", Toast.LENGTH_SHORT).show()
    }

    companion object {
//        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
        const val REQUEST_TAKE_PHOTO = 1
        const val PERMISSION_CALLBACK_CONSTANT = 100
    }
}