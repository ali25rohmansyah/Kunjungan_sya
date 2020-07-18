package com.raproject.kunjungan.view.ui.home

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
    private val crScope = CoroutineScope(vmJob + Dispatchers.Main)

    init {
        _response.postValue("1")
        crScope.launch {
            try {
                val result = kunjunganService.kunjunganApi.retrofitService.showList()
                if (result.isNotEmpty()) {
                    _items.value = result
                    _response.postValue("2")
                }
            }catch (t: Throwable){
                _response.postValue(t.message.toString())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
    }
}