package com.jamescoggan.workshopapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.jamescoggan.workshopapp.port.gpioForButton
import com.jamescoggan.workshopapp.port.gpioForLED
import com.jamescoggan.workshopapp.port.i2cForTempSensor
import com.jamescoggan.workshopapp.sensors.OnStateChangeListener
import com.jamescoggan.workshopapp.sensors.Sensor
import com.jamescoggan.workshopapp.sensors.TemperatureSensor
import timber.log.Timber

class ThingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_things)
    }

    override fun onStart() {
        super.onStart()
        // Place your code on activity start here
    }

    override fun onStop() {
        // Place your code on activity stop here
        super.onStop()
    }
}
