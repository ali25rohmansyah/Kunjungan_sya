package com.raproject.kunjungan.view.ui.detailUser

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raproject.kunjungan.network.DetailUserData
import com.raproject.kunjungan.network.UserData
import com.raproject.kunjungan.network.kunjunganService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetailUserViewModel (id: String, application: Application): AndroidViewModel(application) {

    private var _response = MutableLiveData<String>()
    private var _items = MutableLiveData<DetailUserData>()
    private val id = id

    val response: LiveData<String>
        get() =_response
    val items: LiveData<DetailUserData>
        get() = _items

    private val vmJob = Job()
    private val crScope = CoroutineScope(vmJob + Dispatchers.Main)

    init {
        showDetailUser()
    }

    fun showDetailUser(){
        _response.postValue("1")
        crScope.launch {
            try {
                val result = kunjunganService.kunjunganApi.retrofitService.detailUser(id)

                _items.value = result
                _response.postValue("2")

            }catch (t: Throwable){
                Log.d("debug", t.message.toString())
            }
        }
    }

    fun updateUser(nik: String, foto: String, nama: String, tanggal_lahir: String,alamat: String, no_hp: String, status_no_hp: String, status_kepegawaian: String, gaji: Int, pembayaran_gaji: String, sales_respon: String){
        vmJob.cancel()
        _response.postValue("3")
        crScope.launch {
            try {
                kunjunganService.kunjunganApi.retrofitService.updateUser(
                    id,
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
                    sales_respon)

                _response.postValue("4")

            }catch (t: Throwable){
                Log.d("debug", t.message.toString())
            }
        }
    }

//    override fun onCleared() {
//        super.onCleared()
//        vmJob.cancel()
//    }



}