package com.example.stepbystep

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log

class SensorProducer(
    ctx: Context,
    private val mSensorSharedData: SharedData,
    private val mHandler: Handler,
    samplingWindowSize: Int,
    samplingPeriodMs: Int
) : SensorEventListener {
    companion object {
        const val SAMPLING_RANGE = 2_000_000
        private const val TAG = "SensorEventProducer"
    }

    // Constants obtained from configuration converted to microseconds
    private val mSamplingPeriodUs = samplingPeriodMs * 1_000
    private val mMaxLatencyUS = mSamplingPeriodUs * samplingWindowSize

    // Sensor manager and sensors
    private val mSensorManager: SensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var mAccSensor: Sensor? = null
    private var mGyroSensor: Sensor? = null
    private var mIsListening = false

    private var mLastAccTs: Long? = null
    private var mLastGyroTs: Long? = null

    /**
     * Minimum value for the sampling rate range in nanoseconds.
     */
    private val minSamplingRange
        get() = (mSamplingPeriodUs * 1_000) - SAMPLING_RANGE

    /**
     * Maximum value for the sampling rate range in nanoseconds.
     */
    private val maxSamplingRange
        get() = (mSamplingPeriodUs * 1_000) + SAMPLING_RANGE

    init {
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        if (mAccSensor == null) {
            Log.w(TAG, "Accelerometer sensor is not supported!")
        }

        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (mGyroSensor == null) {
            Log.w(TAG, "Gyroscope sensor is not supported!")
        }
    }

    /**
     * Start listening to sensors events.
     *
     * This method starts listening to the accelerometer and gyroscope
     * sensors if they are supported and we are not already listening.
     *
     * @see [stopListening].
     */
    fun startListening() {
        if (!mIsListening) {
            mIsListening = true
            if (mAccSensor != null) {
                mSensorManager.registerListener(
                    this,
                    mAccSensor,
                    mSamplingPeriodUs,
                    mMaxLatencyUS,
                    mHandler
                )
            }
            if (mGyroSensor != null) {
                mSensorManager.registerListener(
                    this,
                    mGyroSensor,
                    mSamplingPeriodUs,
                    mMaxLatencyUS,
                    mHandler
                )
            }
            Log.d(TAG, "startListening: start listening to sensors!")
        }
    }

    /**
     * Stop listening to sensors events.
     *
     * This method is safe to call as many times we want,
     * even before [startListening].
     *
     * @see [startListening].
     */
    fun stopListening() {
        mIsListening = false
        mSensorManager.unregisterListener(this)
        Log.d(TAG, "stopListening: stop listening to sensors!")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                // The first time mLastAccTs is null, we accept the sample no matter what
                if (mLastAccTs == null) {
                    mLastAccTs = event.timestamp
                    mSensorSharedData.putAcc(SensorSample.fromArray(event.values))
                } else {
                    // Compute the elapsed time
                    val elapsed = event.timestamp - mLastAccTs!!
                    if (elapsed >= minSamplingRange) {
                        // The elapsed time is >= than the minSamplingRange, so we accept it
                        mLastAccTs = event.timestamp
                        mSensorSharedData.putAcc(SensorSample.fromArray(event.values))
                        if (elapsed > maxSamplingRange) {
                            Log.d(
                                TAG,
                                "onSensorChanged: got a late accelerometer event '${elapsed}ns'"
                            )
                        }
                    }
                    // The elapsed time should not be negative... something strange happened
                    if (elapsed < 0) {
                        Log.wtf(
                            TAG,
                            "onSensorChanged: got an accelerometer event with negative elapsed time '${elapsed}ns'"
                        )
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                // The first time mLastGyroTs is null, we accept the sample no matter what
                if (mLastGyroTs == null) {
                    mLastGyroTs = event.timestamp
                    mSensorSharedData.putGyro(SensorSample.fromArray(event.values))
                } else {
                    // Compute the elapsed time
                    val elapsed = event.timestamp - mLastGyroTs!!
                    if (elapsed >= minSamplingRange) {
                        // The elapsed time is >= than the minSamplingRange, so we accept it
                        mLastGyroTs = event.timestamp
                        mSensorSharedData.putGyro(SensorSample.fromArray(event.values))
                        if (elapsed > maxSamplingRange) {
                            Log.d(
                                TAG,
                                "onSensorChanged: got a late gyroscope event '${elapsed}ns'"
                            )
                        }
                    }
                    // The elapsed time should not be negative... something strange happened
                    if (elapsed < 0) {
                        Log.wtf(
                            TAG,
                            "onSensorChanged: got an gyroscope event with negative elapsed time '${elapsed}ns'"
                        )
                    }
                }
            }
            else -> {
                Log.w(TAG, "onSensorChanged: Unknown sensor type ${event?.sensor?.type}")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "onAccuracyChanged: Changed accuracy of sensor '${sensor?.name}' to '$accuracy'")
    }
}