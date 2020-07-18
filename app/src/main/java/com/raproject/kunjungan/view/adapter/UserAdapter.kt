package com.raproject.kunjungan.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.raproject.kunjungan.network.UserData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raproject.kunjungan.R
import com.raproject.kunjungan.databinding.ItemUserBinding

class UserAdapter(private val showDetail: (String) -> Unit): ListAdapter<UserData, UserAdapter.ViewHolder>(DiffCallback){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder{
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_user,
                parent,
                false
            ))
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemUserBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(userData: UserData){
            binding.cardUser.setOnClickListener{
                showDetail(binding.txtNik.text.toString())
            }
            binding.item = userData
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<UserData>(){
        override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return  oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return oldItem.nik == newItem.nik
        }

    }

}