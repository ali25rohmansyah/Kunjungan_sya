package com.raproject.kunjungan.view.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.raproject.kunjungan.R
import com.raproject.kunjungan.databinding.ActivityHomeBinding
import com.raproject.kunjungan.view.adapter.UserAdapter
import com.raproject.kunjungan.view.ui.detailUser.DetailUserActivity
import com.raproject.kunjungan.view.ui.inputUser.InputUserActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        this.viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        //add rv
        val viewAdapter = UserAdapter{item -> showDetail(item)}
        binding.rvUser.adapter = viewAdapter
        viewModel.items.observe(this, Observer { viewAdapter.submitList(it) })

        binding.fab.setOnClickListener{
            startActivity(Intent(this, InputUserActivity::class.java))
            finish()
        }

        viewModel.response.observe(this, Observer {
            if(it == "2"){
                binding.progressCircular.visibility = View.GONE
                binding.rvUser.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
            }else if (it == "1"){
                binding.rvUser.visibility = View.GONE
                binding.fab.visibility = View.GONE
                binding.progressCircular.visibility = View.VISIBLE
            }
        })
    }

    private fun showDetail(id: String){
        val intent = Intent(this, DetailUserActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }
}