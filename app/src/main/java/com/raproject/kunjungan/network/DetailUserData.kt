package com.raproject.kunjungan.network

data class DetailUserData(
    val nik: String,
    val foto: String,
    val nama: String,
    val tanggal_lahir: String,
    val alamat: String,
    val no_hp: String,
    val status_no_hp: String,
    val status_kepegawaian: String,
    val gaji: String,
    val pembayaran_gaji: String,
    val sales_respon: String
)