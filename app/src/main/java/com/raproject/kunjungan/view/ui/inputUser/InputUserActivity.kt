package com.raproject.kunjungan.view.ui.inputUser

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    private var imageUrl: String = "https://firebasestorage.googleapis.com/v0/b/kunjungan-3506f.appspot.com/o/images%2Fboy.png?alt=media&token=44eab7f7-6770-4885-9165-a61682482652"

    //firebase storage
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_input_user)
        this.viewModel = ViewModelProvider(this).get(InputUserViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.edtTanggalLahir.setOnClickListener{showTimeDialog()}

        //save data
        binding.btnSave.setOnClickListener {
            if(binding.edtNik.text.isNotEmpty()) {

                //getData from input user
                val nik = binding.edtNik.text.toString()
                val foto = imageUrl
                val nama = binding.edtNama.text.toString()
                val tanggal_lahir = binding.edtTanggalLahir.text.toString()
                val alamat = binding.edtAlamat.text.toString()
                val no_hp = "+62"+binding.edtNoHp.text.toString()
                val status_no_hp = "1"
                var gaji = 0
                if (binding.edtGaji.text.isNotEmpty()){
                    gaji = binding.edtGaji.text.toString().toInt()
                }
                val bank = binding.spBank.selectedItem.toString()
                val sales_respon = binding.edtSalesRespon.text.toString()
                var statusKepegawaian = ""
                if (binding.rdButton1.isChecked) {
                    statusKepegawaian = "aktif"
                }else if(binding.rdButton1.isChecked){
                    statusKepegawaian = "pensiun"
                }

                viewModel.insertData(nik, foto, nama, tanggal_lahir, alamat, no_hp, status_no_hp, statusKepegawaian, gaji, bank, sales_respon)
                viewModel.response.observe(this, Observer {
                    if (it == "1") {
                        progress_circular.visibility = View.VISIBLE
                    } else if (it == "2") {
                        progress_circular.visibility = View.GONE
                        finish()
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                })
            }else{
                Toast.makeText(this, "NIK harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.response.observe(this, Observer {
            if (it == "2"){
                finish()
            }
        })

        //OTP
        mAuth = FirebaseAuth.getInstance()
        binding.btnOtp.setOnClickListener{
            requestOtp("+62"+binding.edtNoHp.text.toString())
            binding.btnValidasiOtp.visibility = View.VISIBLE
        }

        //OTP verification
        binding.btnValidasiOtp.setOnClickListener{
            val credential = PhoneAuthProvider.getCredential(mVerificationId, binding.edtOtp.text.toString())
            signInWithPhoneAuthCredential(credential)

        }

        storage = FirebaseStorage.getInstance();
        storageReference = storage.reference;
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

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    //Check permissions
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

                filePath = Uri.fromFile(File(photoPath!!))
                val photo: Bitmap = BitmapFactory.decodeFile(photoPath)
                binding.ivProfile.setImageBitmap(photo)

                val ref = storageReference.child("images/" + UUID.randomUUID().toString())
                ref.putFile(filePath)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            imageUrl = it.toString()
                            Log.d("debug", "foto$imageUrl")
                            Log.d("debug", it.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "upload foto gagal", Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener {
                    }
            }else{
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/kunjungan-3506f.appspot.com/o/images%2Fboy.png?alt=media&token=44eab7f7-6770-4885-9165-a61682482652"
            }
        }
    }

    private fun showTimeDialog() {

        val c = Calendar.getInstance();
        val mYear = c.get(Calendar.YEAR);
        val mMonth = c.get(Calendar.MONTH);
        val mDay = c.get(Calendar.DAY_OF_MONTH);

        val datePickerDialog = DatePickerDialog(
            this,
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                binding.edtTanggalLahir.setText("$dayOfMonth-$monthOfYear-$year")
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("otp", "onVerificationCompleted:$credential")
            binding.btnValidasiOtp.visibility = View.GONE
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.d("otp", "onVerificationFailed: $e")

            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("debug",e.toString())

            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("otp", e.toString())

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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
    }

    companion object {
//        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
        const val REQUEST_TAKE_PHOTO = 1
        const val PERMISSION_CALLBACK_CONSTANT = 100
    }
}