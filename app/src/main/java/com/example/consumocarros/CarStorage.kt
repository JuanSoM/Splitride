package com.example.consumocarros

import android.content.Context
import org.json.JSONArray

object CarStorage {
    private const val PREFS_NAME = "mis_coches"
    private const val KEY_CARS = "cars"

    fun getCars(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CARS, "[]")
        val jsonArray = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }

    fun addCar(context: Context, car: String) {
        val cars = getCars(context).toMutableList()
        cars.add(car)
        saveCars(context, cars)
    }

    fun removeCar(context: Context, car: String) {
        val cars = getCars(context).toMutableList()
        cars.remove(car)
        saveCars(context, cars)
    }

    private fun saveCars(context: Context, cars: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(cars)
        prefs.edit().putString(KEY_CARS, jsonArray.toString()).apply()
    }
}
