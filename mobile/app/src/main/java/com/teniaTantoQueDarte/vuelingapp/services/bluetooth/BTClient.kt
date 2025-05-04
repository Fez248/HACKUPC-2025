package com.teniaTantoQueDarte.vuelingapp.services.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BTClient(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val callback: BluetoothConnectionCallback
) {
    private val bluetoothAdapter = bluetoothManager.adapter
    private var socket: BluetoothSocket? = null
    private var isConnected = false
    private var connectThread: Thread? = null
    private var communicationThread: Thread? = null
    private var targetDeviceCode: String = ""
    private val foundDevices = mutableListOf<BluetoothDevice>()

    private val receiver = object : BroadcastReceiver() {
        @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_SCAN"])
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    device?.let {
                        foundDevices.add(it)
                        val deviceName = it.name ?: "Unknown"
                        Log.d(TAG, "Found device: $deviceName (${it.address})")

                        if (deviceName.contains(targetDeviceCode)) {
                            Log.d(TAG, "Found target device: $deviceName")
                            stopDiscovery()
                            connectToDevice(it)
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    // Handle pairing state changes if needed
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun connect(deviceCode: String): Boolean {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter not available")
            return false
        }

        targetDeviceCode = deviceCode
        foundDevices.clear()

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)

        return if (bluetoothAdapter.startDiscovery()) {
            Log.d(TAG, "Discovery started")
            true
        } else {
            Log.e(TAG, "Failed to start discovery")
            context.unregisterReceiver(receiver)
            false
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    private fun stopDiscovery() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }

        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToDevice(device: BluetoothDevice) {
        if (isConnected) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()

        connectThread = Thread {
            try {
                socket = device.createRfcommSocketToServiceRecord(BTServer.SERVICE_UUID)
                socket?.connect()

                isConnected = true
                Log.d(TAG, "Connected to device: ${device.address}")

                callback.onDeviceConnected(device.address)
                startCommunication()
            } catch (e: IOException) {
                Log.e(TAG, "Socket connect() failed", e)
                callback.onError(ERROR_CONNECTION, "Failed to connect: ${e.message}")

                try {
                    socket?.close()
                } catch (closeException: IOException) {
                    Log.e(TAG, "Error closing socket", closeException)
                }
                isConnected = false
            }
        }
        connectThread?.start()
    }

    private fun startCommunication() {
        communicationThread = Thread {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (isConnected && socket?.isConnected == true) {
                try {
                    val inputStream: InputStream = socket!!.inputStream
                    bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val receivedData = buffer.copyOfRange(0, bytes)
                        callback.onDataReceived(receivedData)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Communication disconnected", e)
                    if (isConnected) {
                        socket?.remoteDevice?.address?.let { callback.onDeviceDisconnected(it) }
                    }
                    break
                }
            }
        }
        communicationThread?.start()
    }

    fun sendData(data: ByteArray): Boolean {
        if (!isConnected || socket == null || !socket!!.isConnected) {
            return false
        }

        return try {
            val outputStream: OutputStream = socket!!.outputStream
            outputStream.write(data)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error sending data", e)
            false
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun disconnect() {
        isConnected = false

        stopDiscovery()

        communicationThread?.interrupt()
        communicationThread = null

        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket", e)
        }
        socket = null

        connectThread?.interrupt()
        connectThread = null
    }

    companion object {
        private const val TAG = "BluetoothClient"
        private const val ERROR_CONNECTION = 1
        private const val ERROR_COMMUNICATION = 2
    }
}