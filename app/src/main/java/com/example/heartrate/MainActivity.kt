package com.example.heartrate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartSensor: Sensor? = null

    override fun onSensorChanged(event: SensorEvent) {
        val rate = event.values[0]
        textView.text = "Rate: $rate"
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_NO_CONTACT -> textView.text = "No contact"
            SensorManager.SENSOR_STATUS_UNRELIABLE -> textView.text = "Unreliable"
            else -> textView.text = "OK accuracy"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
