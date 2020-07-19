package com.raproject.kunjungan.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val baseUrl = "https://raasjack.000webhostapp.com/"

val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(baseUrl)
    .build()

//Api interface / Endpoint
interface kunjunganService{
    @GET("/user")
    suspend fun showList():
            List<UserData>

    @GET("user/{id}")
    suspend fun detailUser(@Path("id")id: String):
            DetailUserData

    @FormUrlEncoded
    @POST("/user")
    suspend fun insertUser(
        @Field("nik")nik: String,
        @Field("foto")foto: String,
        @Field("nama")nama: String,
        @Field("tanggal_lahir")tanggal_lahir: String,
        @Field("alamat")alamat: String,
        @Field("no_hp")no_hp: String,
        @Field("status_no_hp")status_no_hp: String,
        @Field("status_kepegawaian")status_kepegawaian: String,
        @Field("gaji")gaji: Int,
        @Field("pembayaran_gaji")pembayaran_gaji: String,
        @Field("sales_respon")sales_respon: String
    ): DetailUserData

    @FormUrlEncoded
    @POST("/user/update/{id}")
    suspend fun updateUser(
        @Path("id")id: String,
        @Field("nik")nik: String,
        @Field("foto")foto: String,
        @Field("nama")nama: String,
        @Field("tanggal_lahir")tanggal_lahir: String,
        @Field("alamat")alamat: String,
        @Field("no_hp")no_hp: String,
        @Field("status_no_hp")status_no_hp: String,
        @Field("status_kepegawaian")status_kepegawaian: String,
        @Field("gaji")gaji: Int,
        @Field("pembayaran_gaji")pembayaran_gaji: String,
        @Field("sales_respon")sales_respon: String
    ): DetailUserData

    @GET("/user/delete/{id}")
    suspend fun deleteUser(@Path("id")id: String):
            DetailUserData
    
    @GET("user/find/{id}")
    suspend fun findUser(@Path("id")id: String):
            List<UserData>

object kunjunganApi{
    val retrofitService: kunjunganService = retrofit.create(kunjunganService::class.java)
}
}