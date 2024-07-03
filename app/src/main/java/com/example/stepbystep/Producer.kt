package com.example.stepbystep

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


class Producer(context: Context, b: Buffer) : SensorEventListener, Runnable {
    private val buffer = b

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val accelerometerData = FloatArray(3)
    private val gyroscopeData = FloatArray(3)

    private val sensorDataFlow = MutableSharedFlow<SensorData>()

    override fun run() {
        var isAlive: Boolean = true
        sensorManager.registerListener(this, accelerometer, 100000)
        sensorManager.registerListener(this, gyroscope, 100000)

        //CAPIRE COME FERMARE IL TUTTO, con l'interrupt per poi togliere i sensori registrati

        //buffer.put(SensorData(accelerometerData.copyOf(), gyroscopeData.copyOf()))
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

       // buffer.put(SensorData(accelerometerData.copyOf(), gyroscopeData.copyOf()))
        // Emit the combined sensor data
        CoroutineScope(Dispatchers.Default).launch {
          //  sensorDataFlow.emit(SensorData(accelerometerData.copyOf(), gyroscopeData.copyOf()))
            buffer.put(SensorData(accelerometerData.copyOf(), gyroscopeData.copyOf()))
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }


}