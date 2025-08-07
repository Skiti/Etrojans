package com.malicious.scootertoolkit.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.content.Context
import android.util.Log
import com.malicious.scootertoolkit.xiaomi.BleConstant
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.ArrayList
import java.util.LinkedList
import java.util.Queue
import java.util.UUID


class BleManagerAdapter(context: Context?) :
    BleManager(context!!) {


    companion object {
        private const val TAG = "BleManager"
    }

    private val uartTxQueue: Queue<ByteArray> = LinkedList<ByteArray>()
    private val upnpQueue: Queue<ByteArray> = LinkedList<ByteArray>()
    private val avdtpTxQueue: Queue<ByteArray> = LinkedList<ByteArray>()

    // ==== Logging =====
    override fun getMinLogPriority(): Int {
        // Use to return minimal desired logging priority.
        return Log.VERBOSE
    }

    override fun log(priority: Int, message: String) {
        // Log from here.
        Log.println(priority, TAG, message)
    }

    // ==== Required implementation ====
    // This is a reference to a characteristic that the manager will use internally.

    private var TXControlPoint: BluetoothGattCharacteristic? = null
    private var RXControlPoint: BluetoothGattCharacteristic? = null

    private var UPNPControlPoint : BluetoothGattCharacteristic? = null
    private var AVDTPControlPoint : BluetoothGattCharacteristic? = null
    private var Unk1ControlPoint : BluetoothGattCharacteristic? = null

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        // Here obtain instances of your characteristics.
        // Return false if a required service has not been discovered.
        val UARTService = gatt.getService(BleConstant.UART_service)
        val AUTHService = gatt.getService(BleConstant.AUTH_service)

        if (UARTService != null && AUTHService != null) {
            TXControlPoint = UARTService.getCharacteristic(BleConstant.UART_TX_characteristic)
            RXControlPoint = UARTService.getCharacteristic(BleConstant.UART_RX_characteristic)

            UPNPControlPoint = AUTHService.getCharacteristic(BleConstant.AUTH_UPNP_characteristic)
            AVDTPControlPoint = AUTHService.getCharacteristic(BleConstant.AUTH_AVDTP_characteristic)
            Unk1ControlPoint = AUTHService.getCharacteristic(BleConstant.AUTH_UNK1_characteristic)
        }
        return TXControlPoint != null && RXControlPoint != null && UPNPControlPoint != null && AVDTPControlPoint != null && Unk1ControlPoint != null
    }

    override fun initialize() {
        // Initialize your device.
        // This means e.g. enabling notifications, setting notification callbacks, or writing
        // something to a Control Point characteristic.
        // Kotlin projects should not use suspend methods here, as this method does not suspend.
        requestMtu(512).enqueue()
        beginAtomicRequestQueue()
            .add(enableNotifications(UPNPControlPoint)
                .fail { _: BluetoothDevice?, status: Int ->
                    log(Log.ERROR, "Could not subscribe UPNP: $status")
                    disconnect().enqueue()
                }
            )
            .add(enableNotifications(AVDTPControlPoint)
                .fail { _: BluetoothDevice?, status: Int ->
                    log(Log.ERROR, "Could not subscribe AVDTP: $status")
                    disconnect().enqueue()
                }
            )
            .add(enableNotifications(TXControlPoint)
                .fail { _: BluetoothDevice?, status: Int ->
                    log(Log.ERROR, "Could not subscribe TX: $status")
                    disconnect().enqueue()
                }
            )
            .done {
                log(Log.INFO, "Target initialized")
            }
            .enqueue()
    }

    override fun onServicesInvalidated() {
        // This method is called when the services get invalidated, i.e. when the device
        // disconnects.
        // References to characteristics should be nullified here.
        TXControlPoint = null
        RXControlPoint = null
        UPNPControlPoint = null
        AVDTPControlPoint = null
        Unk1ControlPoint = null
    }



    private fun dataWrite(r : Data): ByteArray? {
        if (r.value != null) {
            log(Log.INFO,"Data written: $r.value")
            return r.value
        }
        return null
    }

    // ==== Public API ====
    // Here you may add some high level methods for your device:
    fun write(uuid: UUID?, data: ByteArray?, onWriteSuccess: Consumer<ByteArray>? ) {
        when (uuid) {

            BleConstant.UART_RX_characteristic -> writeCharacteristic(RXControlPoint, data, WRITE_TYPE_NO_RESPONSE).with { _, r ->
                dataWrite(r)?.let { onWriteSuccess?.accept(it) }
            }.enqueue()
            BleConstant.AUTH_UPNP_characteristic -> writeCharacteristic(UPNPControlPoint, data, WRITE_TYPE_NO_RESPONSE).with { _, r ->
                dataWrite(r)?.let { onWriteSuccess?.accept(it) }
            }.enqueue()
            BleConstant.AUTH_AVDTP_characteristic -> writeCharacteristic(AVDTPControlPoint, data, WRITE_TYPE_NO_RESPONSE).with { _, r ->
            dataWrite(r)?.let { onWriteSuccess?.accept(it) }
            }.enqueue()

        }
    }

    fun write(uuid: UUID?, data: ArrayList<ByteArray>?, onWriteSuccess: Consumer<ByteArray>? ) {
        if (data != null) {
            for (d in data){
                write(uuid,d,onWriteSuccess)
            }
        }
    }

    fun read(uuid: UUID?) : ByteArray? {
        var response : ByteArray? = null
        when (uuid) {
            BleConstant.AUTH_UNK1_characteristic -> readCharacteristic(Unk1ControlPoint).with { _, r -> response = dataWrite(r) }.enqueue()
        }
        return response
    }

    fun onNotify(uuid: UUID?) : Observable<ByteArray> {
        Log.d(TAG, uuid.toString())

        return Observable.create<ByteArray> { emitter ->
            when(uuid) {
                BleConstant.UART_TX_characteristic -> {
                    setNotificationCallback(TXControlPoint).with { _, data ->
                        dataWrite(data)?.let { emitter.onNext(it) }
                    }
                }
                BleConstant.AUTH_UPNP_characteristic -> {
                    setNotificationCallback(UPNPControlPoint).with { _, data ->
                        dataWrite(data)?.let { emitter.onNext(it) }
                    }
                }
                BleConstant.AUTH_AVDTP_characteristic -> {
                    setNotificationCallback(AVDTPControlPoint).with { _, data ->
                        dataWrite(data)?.let { emitter.onNext(it) }
                    }
                }
                else ->  Log.d(TAG,"Missing characteristic")
            }
        }
    }





}