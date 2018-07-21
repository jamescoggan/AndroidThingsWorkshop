package com.jamescoggan.workshopapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamescoggan.workshopapp.model.HomeInformation
import com.jamescoggan.workshopapp.port.gpioForButton
import com.jamescoggan.workshopapp.port.gpioForLED
import com.jamescoggan.workshopapp.port.i2cForTempSensor
import com.jamescoggan.workshopapp.sensors.TemperatureSensor
import timber.log.Timber

class ThingsActivity : AppCompatActivity() {

    private lateinit var led: Gpio
    private lateinit var button: Gpio
    private var dbReference: DatabaseReference? = null
    private val tempSensor = TemperatureSensor(i2cForTempSensor, 2000L) // Read temperature every 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_things)
    }

    override fun onStart() {
        super.onStart()

        led = PeripheralManager.getInstance().openGpio(gpioForLED) // Open the LED port
        led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) // Set the port to OUT with initial LOW
        button = PeripheralManager.getInstance().openGpio(gpioForButton) // Open the Button Port
        button.setDirection(Gpio.DIRECTION_IN) // Set the port to IN for reading
        button.setActiveType(Gpio.ACTIVE_LOW) // Active type is low, when button is grounded
        button.setEdgeTriggerType(Gpio.EDGE_BOTH) // We want to detect on press and release
        button.registerGpioCallback { gpio ->
            Timber.d("Button pressed: ${gpio.value}") // Read and print the GPIO value
            dbReference?.child("light")?.setValue(gpio.value)
            dbReference?.child("button")?.setValue(gpio.value)
            true // Return true so we continue monitoring the button events
        }

        tempSensor.open() // open the port
        tempSensor.setListener { value: Int ->
            Timber.d("Current temperature $value")
            dbReference?.child("temperature")?.setValue(value)
        }

        loginFirebase()
    }

    private fun loginFirebase() {
        val firebase = FirebaseAuth.getInstance()
        firebase.signInAnonymously()
                .addOnSuccessListener {
                    Timber.d("Firebase logged in successfully")
                    dbReference = FirebaseDatabase.getInstance().reference.child("home")
                    observeChanges()
                }
                .addOnFailureListener { Timber.e("Failed to login $it") }
    }

    private fun observeChanges() {
        dbReference?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Timber.e("Something has gone wrong: ${error.message}")
            }

            override fun onDataChange(homeValue: DataSnapshot) {
                val homeInformation = homeValue.getValue(HomeInformation::class.java) // Convert the value to the HomeInformation data class
                Timber.d("New value received: $homeInformation")
                homeInformation?.let { // If homeInformation not null
                    led.value = it.light // Set the LED to the light boolean value
                }
            }

        })
    }

    override fun onStop() {
        led.close()
        button.close()
        tempSensor.close()
        super.onStop()
    }
}
