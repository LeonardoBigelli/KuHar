package com.example.stepbystep

import android.util.Log

/**
 * Used for receiving notifications when there is new windowed
 * data from the sensors.
 *
 * This interface needs to be implemented by any class that want
 * to do something with the data received from the sensors.
 *
 * @see[SensorSharedData]
 * @see[SensorEventProducer]
 * @see[SensorEventConsumerRn]
 */
fun interface SensorDataHandler {
    /**
     * Called when there is a new batch of sensors data.
     *
     * This method is call every time the [SensorEventConsumerRn] is able
     * to consume an entire window of sensors data. The data passed as a
     * parameter is a 2d array flattened into a 1d array in row-major order.
     *
     * @param[data] Array of sensors values.
     */
    fun onNewData(data: FloatArray)
}
/**
 * A consumer of sensors values in a dedicated thread.
 *
 * This class waits for [SensorSharedData] to produce a full list of
 * sensors values and then calls [SensorDataHandler] interface with it.
 * All the work is done in a dedicated thread that the user needs to spawn.
 *
 * Example usage:
 * ```
 * // implement the SensorDataHandler interface with user's logic
 * val handler: SensorDataHandler = object : SensorDataHandler {
 *      override fun onNewData(data: FloatArray) {
 *          // do something with data
 *      }
 * }
 * // create a SensorEventConsumerRn
 * val c: SensorEventConsumerRn = SensorEventConsumerRn(data, handler)
 * // create a new Thread
 * val t: Thread = Thread(c, "SensorEventConsumerThread")
 * // later start the thread using
 * t.start()
 * // and stop it using
 * t.interrupt()
 * ```
 *
 * @property[mData] A shared [SensorSharedData] that contains the values needed.
 * @property[mHandler] A class that implements [SensorDataHandler] to do something with the
 * collected data.
 */
class SensorConsumer(
    private val mData: SharedData,
    private val mHandler: SensorDataHandler
) : Runnable {
    companion object {
        private const val TAG = "SensorsConsumerRunnable"
    }

    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                // Get the new data and pass it to SensorsConsumer implementation
                mHandler.onNewData(mData.getData())
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        Log.d(TAG, "run: ${Thread.currentThread().name} stopped!")
    }
}