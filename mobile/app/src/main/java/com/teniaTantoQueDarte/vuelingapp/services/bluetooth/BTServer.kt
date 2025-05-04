package com.teniaTantoQueDarte.vuelingapp.services.bluetooth

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BTServer(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val callback: BluetoothConnectionCallback
) {
    private val bluetoothAdapter = bluetoothManager.adapter
    private var serverSocket: BluetoothServerSocket? = null
    private var socket: BluetoothSocket? = null
    private var serverThread: Thread? = null
    private var communicationThread: Thread? = null
    private var isRunning = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun start(deviceName: String): Boolean {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter not available")
            return false
        }

        // Close existing server if any
        stop()

        try {
            bluetoothAdapter.name = deviceName
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                deviceName,
                SERVICE_UUID
            )
        } catch (e: IOException) {
            Log.e(TAG, "Socket listen() failed", e)
            callback.onError(ERROR_SERVER_SOCKET, "Failed to create server socket: ${e.message}")
            return false
        }

        isRunning = true
        serverThread = Thread {
            while (isRunning) {
                try {
                    Log.d(TAG, "Server socket waiting for connection...")
                    socket = serverSocket?.accept()

                    socket?.let { connectedSocket ->
                        val deviceAddress = connectedSocket.remoteDevice.address
                        Log.d(TAG, "Device connected: $deviceAddress")
                        callback.onDeviceConnected(deviceAddress)

                        // Start communication thread
                        startCommunication(connectedSocket)

                        // Close server socket after accepting connection
                        serverSocket?.close()
                        serverSocket = null
                    }
                } catch (e: IOException) {
                    if (isRunning) {
                        Log.e(TAG, "Server socket accept() failed", e)
                        callback.onError(ERROR_CONNECTION, "Connection accept failed: ${e.message}")
                    }
                    break
                }
            }
        }
        serverThread?.start()
        return true
    }

    private fun startCommunication(socket: BluetoothSocket) {
        communicationThread = Thread {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (isRunning && socket.isConnected) {
                try {
                    val inputStream: InputStream = socket.inputStream
                    bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val receivedData = buffer.copyOfRange(0, bytes)
                        callback.onDataReceived(receivedData)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Communication disconnected", e)
                    if (isRunning) {
                        callback.onDeviceDisconnected(socket.remoteDevice.address)
                    }
                    break
                }
            }
        }
        communicationThread?.start()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stop() {
        isRunning = false

        communicationThread?.interrupt()
        communicationThread = null

        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket", e)
        }
        socket = null

        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing server socket", e)
        }
        serverSocket = null

        serverThread?.interrupt()
        serverThread = null
    }

    fun sendData(data: ByteArray): Boolean {
        if (!isRunning || socket == null || !socket!!.isConnected) {
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

    companion object {
        private const val TAG = "BluetoothServer"
        private const val ERROR_SERVER_SOCKET = 1
        private const val ERROR_CONNECTION = 2

        val SERVICE_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        const val SERVICE_NAME = "VuelingBluetoothSync"
    }
}