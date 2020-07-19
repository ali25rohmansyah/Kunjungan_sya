package com.raproject.kunjungan.view.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raproject.kunjungan.network.UserData
import com.raproject.kunjungan.network.kunjunganService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    private var _response = MutableLiveData<String>()
    private var _items = MutableLiveData<List<UserData>>()

    val response: LiveData<String>
        get() =_response
    val items: LiveData<List<UserData>>
        get() = _items

    private val vmJob = Job()
    private val vmJob2 = Job()
    private val crScope = CoroutineScope(vmJob + Dispatchers.Main)
    private val crScope2 = CoroutineScope(vmJob2 + Dispatchers.Main)

    init {
        listUser()
    }

    fun listUser(){
        _response.postValue("1")
        crScope.launch {
            try {
                val result = kunjunganService.kunjunganApi.retrofitService.showList()
                if (result.isNotEmpty()) {
                    _items.value = result
                    _response.postValue("2")
                }else{
                    _response.postValue("3")
                }
            }catch (t: Throwable){
                Log.e("error12", t.message.toString())
                _response.postValue("4")
            }
        }
    }

    fun searchUser(query: String){
        _response.postValue("1")
        crScope2.launch {
            try {
                val result = kunjunganService.kunjunganApi.retrofitService.findUser(query)
                if (result.isNotEmpty()) {
                    _items.value = result
                    _response.postValue("2")
                }else{
                    _response.postValue("3")
                }
            }catch (t: Throwable){
                Log.e("error12", t.message.toString())
                _response.postValue("4")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
        vmJob2.cancel()
    }
}