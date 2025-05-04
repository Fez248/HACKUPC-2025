package com.teniaTantoQueDarte.vuelingapp.services.bluetooth

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat

interface BluetoothConnectionCallback {
    fun onDeviceConnected(deviceAddress: String)
    fun onDeviceDisconnected(deviceAddress: String)
    fun onDataReceived(data: ByteArray)
    fun onError(errorCode: Int, message: String)
}

class BTManager private constructor(context: Context, manager: BluetoothManager) {
    private val appContext = context.applicationContext
    private val bluetoothManager = manager
    private val bluetoothAdapter = bluetoothManager.adapter

    private var bluetoothServer: BTServer? = null
    private var bluetoothClient: BTClient? = null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startServer(deviceCode: String, callback: BluetoothConnectionCallback): Boolean {
        if (!isBluetoothEnabled()) return false

        bluetoothServer = BTServer(appContext, bluetoothManager, callback)
        return bluetoothServer?.start(deviceCode) ?: false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stopServer() {
        bluetoothServer?.stop()
        bluetoothServer = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToServer(deviceCode: String, callback: BluetoothConnectionCallback): Boolean {
        if (!isBluetoothEnabled()) return false

        bluetoothClient = BTClient(appContext, bluetoothManager, callback)
        return bluetoothClient?.connect(deviceCode) ?: false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendData(data: ByteArray): Boolean {
        return bluetoothServer?.sendData(data) ?: bluetoothClient?.sendData(data) ?: false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stopClient() {
        bluetoothClient?.disconnect()
        bluetoothClient = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun cleanup() {
        stopServer()
        stopClient()
    }
    companion object {
        private const val TAG = "BluetoothManager"

        @Volatile private var INSTANCE: BTManager? = null

        fun getInstance(context: Context, manager: BluetoothManager): BTManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BTManager(context, manager).also { INSTANCE = it }
            }
        }
    }
}