package com.example.consumocarros

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CarStorage {
    private const val PREFS_NAME = "MyCarsPrefs"
    private const val KEY_CARS = "cars"

    private val gson = Gson()

    fun getCars(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CARS, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun addCar(context: Context, car: String) {
        val cars = getCars(context).toMutableList()
        cars.add(car)
        saveCars(context, cars)
    }

    fun removeCar(context: Context, car: String) {
        val cars = getCars(context).toMutableList()
        if (cars.remove(car)) {
            saveCars(context, cars)
        }
    }

    private fun saveCars(context: Context, cars: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(cars)
        prefs.edit().putString(KEY_CARS, json).apply()
    }
}
