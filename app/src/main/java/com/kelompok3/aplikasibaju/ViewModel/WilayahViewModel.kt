package com.kelompok3.aplikasibaju.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch // Import kotlinx.coroutines.launch
import android.util.Log
import com.kelompok3.aplikasibaju.Model.*
import com.kelompok3.aplikasibaju.Repository.RetrofitClient

class WilayahViewModel : ViewModel() {

    private val _provinces = MutableLiveData<List<Province>>()
    val provinces: LiveData<List<Province>> get() = _provinces

    private val _regencies = MutableLiveData<List<Regency>>()
    val regencies: LiveData<List<Regency>> get() = _regencies

    private val _districts = MutableLiveData<List<District>>()
    val districts: LiveData<List<District>> get() = _districts

    private val _villages = MutableLiveData<List<Village>>()
    val villages: LiveData<List<Village>> get() = _villages

    fun fetchProvinces() {
        viewModelScope.launch {
            try {
                val fetchedProvinces = RetrofitClient.instance.getProvinces()
                _provinces.value = fetchedProvinces
                Log.d("WilayahVM", "Fetched ${fetchedProvinces.size} provinces successfully.")
            } catch (e: Exception) {
                Log.e("WilayahVM", "Failed to fetch provinces: ${e.message}", e)
            }
        }
    }

    fun fetchRegencies(provinceId: String) {
        viewModelScope.launch {
            try {
                val fetchedRegencies = RetrofitClient.instance.getRegencies(provinceId)
                _regencies.value = fetchedRegencies
                Log.d("WilayahVM", "Fetched ${fetchedRegencies.size} regencies for province $provinceId.")
            } catch (e: Exception) {
                Log.e("WilayahVM", "Failed to fetch regencies for province $provinceId: ${e.message}", e)
            }
        }
    }

    fun fetchDistricts(regencyId: String) {
        viewModelScope.launch {
            try {
                val fetchedDistricts = RetrofitClient.instance.getDistricts(regencyId)
                _districts.value = fetchedDistricts
                Log.d("WilayahVM", "Fetched ${fetchedDistricts.size} districts for regency $regencyId.")
            } catch (e: Exception) {
                Log.e("WilayahVM", "Failed to fetch districts for regency $regencyId: ${e.message}", e)
            }
        }
    }

    fun fetchVillages(districtId: String) {
        viewModelScope.launch {
            try {
                val fetchedVillages = RetrofitClient.instance.getVillages(districtId)
                _villages.value = fetchedVillages
                Log.d("WilayahVM", "Fetched ${fetchedVillages.size} villages for district $districtId.")
            } catch (e: Exception) {
                Log.e("WilayahVM", "Failed to fetch villages for district $districtId: ${e.message}", e)
            }
        }
    }
}
