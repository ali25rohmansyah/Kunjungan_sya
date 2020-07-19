package com.raproject.kunjungan.view.ui.home

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.raproject.kunjungan.R
import com.raproject.kunjungan.databinding.ActivityHomeBinding
import com.raproject.kunjungan.view.adapter.UserAdapter
import com.raproject.kunjungan.view.ui.detailUser.DetailUserActivity
import com.raproject.kunjungan.view.ui.inputUser.InputUserActivity


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
                binding.label.visibility = View.GONE
                binding.progressCircular.visibility = View.GONE
                binding.rvUser.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
            }else if (it == "1"){
                binding.label.visibility = View.GONE
                binding.rvUser.visibility = View.GONE
                binding.fab.visibility = View.GONE
                binding.progressCircular.visibility = View.VISIBLE
            }else if(it == "3"){
                binding.label.visibility = View.VISIBLE
                binding.rvUser.visibility = View.GONE
                binding.fab.visibility = View.VISIBLE
                binding.progressCircular.visibility = View.GONE
            }else if(it == "4"){
                binding.label.visibility = View.VISIBLE
                binding.label.text = "Server error silahkan refresh beberapa saat lagi"
                binding.progressCircular.visibility = View.GONE
            }
        })
    }

    private fun searchUser(query: String) {
        viewModel.searchUser(query)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null){
                    searchUser(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        searchView.setOnCloseListener {
            viewModel.listUser()
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun showDetail(id: String){
        val intent = Intent(this, DetailUserActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }
}