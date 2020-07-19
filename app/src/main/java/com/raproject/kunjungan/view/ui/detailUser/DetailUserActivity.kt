package com.raproject.kunjungan.view.ui.detailUser

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SimpleCursorAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.raproject.kunjungan.R
import com.raproject.kunjungan.databinding.ActivityDetailUserBinding
import com.raproject.kunjungan.view.ui.home.HomeActivity
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
            builder.setMessage("Apakah anda yakin ingin menghapus data, karena data yang diubah akan langsung disimpan di simstem kami")
            builder.setPositiveButton("YES"){ dialog, _ ->
                //getData from input user
                val nik = binding.edtNik.text.toString()
                val foto = imageUrl
                val nama = binding.edtNama.text.toString()
                val tanggal_lahir = binding.edtTanggalLahir.text.toString()
                val alamat = binding.edtAlamat.text.toString()
                val no_hp = binding.edtNoHp.text.toString()
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

                viewModel.updateUser(nik, foto, nama, tanggal_lahir, alamat, no_hp, status_no_hp, statusKepegawaian, gaji, bank, sales_respon)
                dialog.dismiss()
                finish()
                startActivity(Intent(this, HomeActivity::class.java))
            }

            builder.setNegativeButton("No"){
                    dialog, _ ->  dialog.cancel()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi")
            builder.setMessage("Apakah anda yakin ingin mengubah data, karena data yang diubah akan langsung disimpan di simstem kami")

            builder.setPositiveButton("YES"){ dialog, _ ->

                viewModel.deleteUser()
                dialog.dismiss()
                finish()
                startActivity(Intent(this, HomeActivity::class.java))
            }

            builder.setNegativeButton("No"){
                    dialog, which ->  dialog.cancel()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        viewModel.response.observe(this, Observer {
            if (it == "2"){
                val statusKepegawaian = viewModel.items.value?.status_kepegawaian
                if (statusKepegawaian != null) {
                    if (statusKepegawaian == "aktif") {
                        binding.rdGroup.check(R.id.rdButton1)
                    } else {
                        binding.rdGroup.check(R.id.rdButton2)
                    }
                }
                if (viewModel.items.value?.status_kepegawaian != null) {
                    when (viewModel.items.value!!.pembayaran_gaji) {
                        "bni" -> {
                            binding.spBank.setSelection(0)
                        }
                        "mandiri" -> {
                            binding.spBank.setSelection(1)
                        }
                        "bri" -> {
                            binding.spBank.setSelection(2)
                        }
                    }
                }

            }
            if(it == "3"){
                progress_circular.visibility = View.VISIBLE
            }else if (it == "4"){
                progress_circular.visibility = View.GONE
                finish()
            }
        })
    }
}