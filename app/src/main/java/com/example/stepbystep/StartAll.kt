package com.example.stepbystep

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StartAll (context: Context) : SensorEventListener, Runnable{
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val accelerometerData = FloatArray(3)
    private val gyroscopeData = FloatArray(3)
    private val sensorDataList = mutableListOf<SensorData>()
    @Volatile private var running = true


    init {
        sensorManager.registerListener(this, accelerometer, 100000)
        sensorManager.registerListener(this, gyroscope, 100000)
    }


    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                accelerometerData[0] = event.values[0]
                accelerometerData[1] = event.values[1]
                accelerometerData[2] = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeData[0] = event.values[0]
                gyroscopeData[1] = event.values[1]
                gyroscopeData[2] = event.values[2]
            }
        }

        sensorDataList.add(SensorData(accelerometerData.copyOf(), gyroscopeData.copyOf()))

        // Keep only the last 2 seconds of data
        if (sensorDataList.size > 200) { // Assuming 100Hz sampling rate
            sensorDataList.removeAt(0)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }

    fun getLastTwoSecondsData(): List<SensorData> {
        return sensorDataList.toList() // Return a copy of the list to prevent modification
    }

    override fun run() {
        while (running) {
            // Your code to process the sensor data in the background
            val data = getLastTwoSecondsData()
            // Process the data (e.g., pass it to a machine learning model)
            try {
                Thread.sleep(10) // Sleep for a short time to simulate processing
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}