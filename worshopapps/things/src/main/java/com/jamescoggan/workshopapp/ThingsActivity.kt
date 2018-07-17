package com.jamescoggan.workshopapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.jamescoggan.workshopapp.actuators.Actuator
import com.jamescoggan.workshopapp.actuators.Led
import com.jamescoggan.workshopapp.port.gpioForButton
import com.jamescoggan.workshopapp.port.gpioForLED
import com.jamescoggan.workshopapp.port.i2cForTempSensor
import com.jamescoggan.workshopapp.sensors.OnStateChangeListener
import com.jamescoggan.workshopapp.sensors.Sensor
import com.jamescoggan.workshopapp.sensors.Switch
import com.jamescoggan.workshopapp.sensors.TemperatureSensor
import timber.log.Timber

class ThingsActivity : AppCompatActivity() {

    private lateinit var led: Gpio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_things)
    }

    override fun onStart() {
        super.onStart()

        led = PeripheralManager.getInstance().openGpio(gpioForLED) // Open the LED port
        led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) // Set the port to OUT with initial LOW

        while(true){
            val newValue = !led.value // Invert the LED value
            Timber.d("Setting the led to $newValue")
            led.value = newValue
            Thread.sleep(1000) // Wait 1 Second
        }
    }

    override fun onStop() {
        led.close()
        super.onStop()
    }
}
