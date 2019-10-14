package com.example.heartrate

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthPermissionManager

class ConnectionListenerImp : HealthDataStore.ConnectionListener {

    private var main: MainActivity
    private var mStore: HealthDataStore? = null
    private var mKeySet: Set<HealthPermissionManager.PermissionKey>

    constructor(mKeySet: Set<HealthPermissionManager.PermissionKey>, main:MainActivity) {
        this.main = main
        this.mKeySet = mKeySet
    }

    fun setStore(mStore: HealthDataStore) {
        this.mStore = mStore
    }

    override fun onConnected() {
        val pmsManager = HealthPermissionManager(mStore)

        // Check whether the permissions that this application needs are acquired
        val resultMap = pmsManager.isPermissionAcquired(mKeySet)

        if (resultMap.containsValue(java.lang.Boolean.FALSE)) {
            // Request the permission if it is not acquired
            pmsManager.requestPermissions(mKeySet, main)
        } else {
            main.readHRSynchronously()
        }
    }

    override fun onConnectionFailed(p0: HealthConnectionErrorResult?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDisconnected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}