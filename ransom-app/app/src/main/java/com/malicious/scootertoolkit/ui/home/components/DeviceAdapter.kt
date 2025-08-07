package com.malicious.scootertoolkit.ui.home.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.malicious.scootertoolkit.R
import com.malicious.scootertoolkit.ble.components.ScannedDevice
import com.malicious.scootertoolkit.ui.home.HomeViewModel


class DeviceAdapter(context: Context, private val mResId: Int, private val mList: MutableList<ScannedDevice>) : ArrayAdapter<ScannedDevice?>(context, mResId, mList as List<ScannedDevice?>) {
    private val mInflater: LayoutInflater
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val item = getItem(position)
        if (convertView == null) {
            convertView = mInflater.inflate(mResId, null)
        }
        val name = convertView!!.findViewById<View>(R.id.device_name) as TextView
        name.text = item!!.getDisplayName()
        val address = convertView.findViewById<View>(R.id.device_address) as TextView
        address.text = item.getDevice().address
        val rssi = convertView.findViewById<View>(R.id.device_rssi) as TextView
        rssi.text = "RSSI: " + item.getRssi().toString()
        val security = convertView.findViewById<View>(R.id.security_level) as TextView
        security.text = "Security level: " + item.getSecurityLevel().toString()

        return convertView
    }

    /**
     * add or update BluetoothDevice
     */
    fun update(newDevice: ScannedDevice) {

        if (newDevice.getDevice().address == null)// ||  !newDevice.getDisplayName().contains("MIScooter")){
            return

        var contains = false
        for (device in mList) {
            if (newDevice.getDevice().address == device.getDevice().address) {
                contains = true
                device.setRssi(newDevice.getRssi()) // update
                break
            }

        }
        if (!contains) // add new BluetoothDevice
            mList.add(newDevice)

        notifyDataSetChanged()
    }


    init {
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}