package com.raproject.kunjungan.view.ui.inputUser

import android.Manifest
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.telecom.Call
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raproject.kunjungan.network.kunjunganService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.security.auth.callback.Callback


class InputUserViewModel: ViewModel(){
    private var _response = MutableLiveData<String>()

    val response: LiveData<String>
        get() = _response

    private val vmJob = Job()
    private val crScope = CoroutineScope(vmJob + Dispatchers.Main)

    fun insertData(nik: String, foto: String, nama: String, tanggal_lahir: String,alamat: String, no_hp: String, status_no_hp: String, status_kepegawaian: String, gaji: Int, pembayaran_gaji: String, sales_respon: String){
        _response.postValue("1")
        crScope.launch {
            try {
                kunjunganService.kunjunganApi.retrofitService.insertUser(
                    nik,
                    foto,
                    nama,
                    tanggal_lahir,
                    alamat,
                    no_hp,
                    status_no_hp,
                    status_kepegawaian,
                    gaji,
                    pembayaran_gaji,
                    sales_respon
                )
                _response.postValue("2")
            } catch (t: Throwable) {
                Log.e("error", t.message.toString())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
    }
}