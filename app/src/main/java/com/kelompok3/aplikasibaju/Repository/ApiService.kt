package com.kelompok3.aplikasibaju.Repository

import com.kelompok3.aplikasibaju.Model.*
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("provinsi.json")
    suspend fun getProvinces(): List<Province>

    @GET("kabupaten/{provinceId}.json")
    suspend fun getRegencies(@Path("provinceId") provinceId: String): List<Regency>

    @GET("kecamatan/{regencyId}.json")
    suspend fun getDistricts(@Path("regencyId") regencyId: String): List<District>

    @GET("kelurahan/{districtId}.json")
    suspend fun getVillages(@Path("districtId") districtId: String): List<Village>
}
