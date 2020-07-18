package com.raproject.kunjungan.view.ui.detailUser

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class DetailUserViewModelFactory(private val id: String, private val application: Application): ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  DetailUserViewModel(id, application) as T
    }

}