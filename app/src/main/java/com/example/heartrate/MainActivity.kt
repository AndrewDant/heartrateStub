package com.example.heartrate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.samsung.android.sdk.healthdata.HealthConstants
import com.samsung.android.sdk.healthdata.HealthData
import com.samsung.android.sdk.healthdata.HealthDataResolver
import com.samsung.android.sdk.healthdata.HealthDataStore
import kotlinx.android.synthetic.main.activity_main.*
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType
import android.support.v4.app.ActivityCompat.requestPermissions
import com.samsung.android.sdk.healthdata.HealthPermissionManager








class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartSensor: Sensor? = null

    private lateinit var handler: Handler
    private lateinit var checkRate: Runnable

    private lateinit var mInstance: MainActivity
    private var mStore: HealthDataStore? = null
    private lateinit var mKeySet: Set<PermissionKey>


    private var mConnectionListener: ConnectionListenerImp? = null

    override fun onSensorChanged(event: SensorEvent) {
        val rate = event.values[0]
//        textView.text = "Rate: $rate"
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        when (accuracy) {
//            SensorManager.SENSOR_STATUS_NO_CONTACT -> textView.text = "No contact"
//            SensorManager.SENSOR_STATUS_UNRELIABLE -> textView.text = "Unreliable"
//            else -> textView.text = "OK accuracy"
//        }
    }

    fun readHRSynchronously() {
        var resolver = HealthDataResolver(mStore, null);

        var request = HealthDataResolver.ReadRequest.Builder()
            .setDataType(HealthConstants.HeartRate.HEART_RATE)
            .build()

        // Checks the result immediately
        var rdResult = resolver.read(request).await()

        var iterator = rdResult.iterator();
        if (iterator.hasNext()) {
            var data = iterator.next();
            var heartRate = data.getFloat(HealthConstants.HeartRate.HEART_RATE)
            textView.text = "this rate: $heartRate"
        }
        rdResult.close()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mInstance = this
        mKeySet = HashSet()
        (mKeySet as HashSet<PermissionKey>).add(PermissionKey(HealthConstants.HeartRate.HEART_RATE, PermissionType.READ))

        mConnectionListener = ConnectionListenerImp(mKeySet, mInstance)

        // Create a HealthDataStore instance and set its listener
        mStore = HealthDataStore(this, mConnectionListener)
        // Request the connection to the health data store
        mStore?.connectService()

        mConnectionListener?.setStore(mStore!!)


        handler = Handler()
        checkRate = Runnable {
            readHRSynchronously()
            handler.postDelayed(checkRate, 1000)
        }
        handler.postDelayed(checkRate, 1000)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 0)
        }




        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if(heartSensor == null)
            textView.text = "No heart sensor detected."
        else {
            textView.text = "Sensor loaded"
            onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        heartSensor?.also { heart ->
            sensorManager.registerListener(this, heart, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

}
