package com.jamescoggan.workshopapp.sensors

import com.google.android.things.pio.I2cDevice
import com.google.android.things.pio.PeripheralManager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

class TemperatureSensor(private val portName: String, private val refreshTime: Long) {

    companion object {
        private const val I2C_ADDRESS = 0x4A
        private val ZERO = ByteArray(1)
    }

    private var job: Job? = null
    private var i2cDevice: I2cDevice? = null
    private var listener: ((Int) -> Unit)? = null

    fun open() {
        close()
        i2cDevice = PeripheralManager.getInstance().openI2cDevice(portName, I2C_ADDRESS)
        requestData()
    }

    fun setListener(listener: (Int) -> Unit) {
        this.listener = listener
        job?.cancel()
        startJob()
    }

    fun close() {
        job?.cancel()
        listener = null
        i2cDevice?.close().also {
            i2cDevice = null
        }
    }

    private fun requestData() {
        i2cDevice?.let {
            it.write(ZERO, ZERO.size)
            val buffer = ByteArray(1)
            it.read(buffer, 1)
            val temperature: Int = buffer[0].toInt() and 0xff
            listener?.let { it(temperature) }
        }
    }

    private fun startJob() {
        job = launch(CommonPool) {
            while (true) {
                requestData()
                Thread.sleep(refreshTime)
            }
        }
    }
}