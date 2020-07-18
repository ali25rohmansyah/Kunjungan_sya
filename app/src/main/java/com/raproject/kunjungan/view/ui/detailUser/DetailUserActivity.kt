package com.raproject.kunjungan.view.ui.detailUser

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.raproject.kunjungan.R
import com.raproject.kunjungan.databinding.ActivityDetailUserBinding
import kotlinx.android.synthetic.main.activity_home.*

class DetailUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailUserBinding
    private lateinit var viewModel: DetailUserViewModel
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_user)
        binding.lifecycleOwner = this

        //get id
        val id = intent.getStringExtra("id")
        val vmfactory = DetailUserViewModelFactory(id!!, this.application)
        this.viewModel = ViewModelProvider(this, vmfactory).get(DetailUserViewModel::class.java)
        binding.viewModel = viewModel

        binding.btnUpdate.setOnClickListener{
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Konfirmasi")

            builder.setMessage("Apakah anda yakin ingin mengubah data, karena data yang diubah akan langsung disimpan di simstem kami")

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("YES"){dialog, which ->
                val rdGroup = binding.rdGroup.checkedRadioButtonId
                viewModel.updateUser(
                    binding.edtNik.text.toString(),
                    imageUrl,
                    binding.edtNama.text.toString(),
                    binding.edtTanggalLahir.text.toString(),
                    binding.edtAlamat.text.toString(),
                    "+62"+binding.edtNoHp.text.toString(),
                    "1",
                    rdGroup.toString(),
                    binding.edtGaji.text.toString().toInt(),
                    binding.spBank.selectedItem.toString(),
                    binding.edtSalesRespon.text.toString()
                )

                dialog.dismiss()
            }

            builder.setNegativeButton("No"){
                dialog, which ->  dialog.cancel()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        viewModel.response.observe(this, Observer {
            if(it == "3"){
                progress_circular.visibility = View.VISIBLE
            }else if (it == "4"){
                progress_circular.visibility = View.GONE
                finish()
            }
        })
    }
}