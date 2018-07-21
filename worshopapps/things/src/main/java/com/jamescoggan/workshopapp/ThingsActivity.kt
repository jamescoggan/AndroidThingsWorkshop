package com.jamescoggan.workshopapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.I2cDevice
import com.google.android.things.pio.PeripheralManager
import com.jamescoggan.workshopapp.port.gpioForButton
import com.jamescoggan.workshopapp.port.gpioForLED
import com.jamescoggan.workshopapp.port.i2cForTempSensor
import com.jamescoggan.workshopapp.sensors.TemperatureSensor
import timber.log.Timber

class ThingsActivity : AppCompatActivity() {

    private lateinit var led: Gpio
    private lateinit var button: Gpio
    private lateinit var tempSensor : I2cDevice

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
            led.value = !gpio.value // Set the LED to the value, the reading is inverted
            true // Return true so we continue monitoring the button events
        }

        tempSensor = PeripheralManager.getInstance().openI2cDevice(i2cForTempSensor, 0x4A)
        tempSensor.write(ByteArray(1), 1)
        val buffer = ByteArray(1)
        tempSensor.read(buffer, 1)
        val temperature: Int = buffer[0].toInt() and 0xff
        Timber.d("Current temperature: $temperature")
    }

    override fun onStop() {
        led.close()
        button.close()
        tempSensor.close()
        super.onStop()
    }
}
