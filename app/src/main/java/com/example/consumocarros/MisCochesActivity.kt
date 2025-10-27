package com.example.consumocarros

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MisCochesActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var buttonAdd: Button
    private lateinit var buttonDelete: Button
    private lateinit var editTextCar: EditText
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_coches)

        listView = findViewById(R.id.listViewCoches)
        buttonAdd = findViewById(R.id.buttonAdd)
        buttonDelete = findViewById(R.id.buttonDelete)
        editTextCar = findViewById(R.id.editTextCar)

        val coches = CarStorage.getCars(this).toMutableList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, coches)
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.adapter = adapter

        buttonAdd.setOnClickListener {
            val carName = editTextCar.text.toString().trim()
            if (carName.isNotEmpty()) {
                CarStorage.addCar(this, carName)
                adapter.add(carName)
                editTextCar.text.clear()
            }
        }

        buttonDelete.setOnClickListener {
            val position = listView.checkedItemPosition
            if (position != ListView.INVALID_POSITION) {
                val car = adapter.getItem(position)
                CarStorage.removeCar(this, car!!)
                adapter.remove(car)
                listView.clearChoices()
                adapter.notifyDataSetChanged()
            }
        }
    }
}

