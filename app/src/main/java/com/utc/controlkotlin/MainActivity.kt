package com.utc.controlkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

const val REQUEST_ENABLE_BT = 1
class MainActivity : AppCompatActivity() {
    //bluetooth adapter
    lateinit var mBtAdapter: BluetoothAdapter
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? =null

    companion object{
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address:String
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        val conectar = findViewById<Button>(R.id.idConectarBT)
        val prender1 = findViewById<Button>(R.id.idBtnOn1)
        val apagar1 = findViewById<Button>(R.id.idBtnOff1)
        val prender2 = findViewById<Button>(R.id.idBtnOn2)
        val apagar2 = findViewById<Button>(R.id.idBtnOff2)
        val buscar = findViewById<Button>(R.id.idDispositivosBT)
        val dispositivos = findViewById<Spinner>(R.id.spinner2)
        val apagarTodo = findViewById<Button>(R.id.idBtnOffTotal)
        val prenderTodo = findViewById<Button>(R.id.idBtnOnTotal)

        val someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE_BT) {
                Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
            }
        }
        //Inicializacion del bluetooth adapter
        mBtAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        //Checar si esta encendido o apagado
        if (mBtAdapter == null) {
            Toast.makeText(
                this,
                "Bluetooth no está disponible en este dipositivo",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, "Bluetooth está disponible en este dispositivo", Toast.LENGTH_LONG)
                .show()
        }


        //activar.setOnClickListener {
            if (mBtAdapter.isEnabled) {
                //Si ya está activado
                Toast.makeText(this, "Bluetooth ya se encuentra activado", Toast.LENGTH_LONG).show()
            } else {
                //Encender Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("MainActivity", "ActivityCompat#requestPermissions")
                }
                someActivityResultLauncher.launch(enableBtIntent)
            }
        //}
        /*//Boton apagar bluetooth
       /* desactivar.setOnClickListener {*/
            if (!mBtAdapter.isEnabled) {
                //Si ya está desactivado
                Toast.makeText(this, "Bluetooth ya se encuentra desactivado", Toast.LENGTH_LONG)
                    .show()
            } else {
                //Encender Bluetooth
                mBtAdapter.disable()
                Toast.makeText(this, "Se ha desactivado el bluetooth", Toast.LENGTH_LONG).show()
            }
       // }*/

        //Boton dispositivos emparejados
        buscar.setOnClickListener {
            if (mBtAdapter.isEnabled) {

                val pairedDevices: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
                mAddressDevices!!.clear()
                mNameDevices!!.clear()

                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mAddressDevices!!.add(deviceHardwareAddress)
                    //........... EN ESTE PUNTO GUARDO LOS NOMBRE A MOSTRARSE EN EL COMBO BOX
                    mNameDevices!!.add(deviceName)
                }

                //ACTUALIZO LOS DISPOSITIVOS
                dispositivos.setAdapter(mNameDevices)
            } else {
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(this, "Primero vincule un dispositivo bluetooth", Toast.LENGTH_LONG)
                    .show()
            }
        }

        conectar.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {

                    val IntValSpin = dispositivos.selectedItemPosition
                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, m_address, Toast.LENGTH_LONG).show()
                    // Cancel discovery because it otherwise slows down the connection.
                    mBtAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                }

                Toast.makeText(this, "CONEXION EXITOSA", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "CONEXION EXITOSA")

            } catch (e: IOException) {
                //connectSuccess = false
                e.printStackTrace()
                Toast.makeText(this, "ERROR DE CONEXION", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "ERROR DE CONEXION")
            }
        }

        prender1.setOnClickListener {
            sendCommand("A")
            //Toast.makeText(this, "ENCENDER", Toast.LENGTH_LONG).show()
        }

        apagar1.setOnClickListener {
            sendCommand("B")
        }
        prender2.setOnClickListener {
            sendCommand("C")
            //Toast.makeText(this, "ENCENDER", Toast.LENGTH_LONG).show()
        }

        apagar2.setOnClickListener {
            sendCommand("D")
        }
        apagarTodo.setOnClickListener{
            sendCommand("Z")
        }
        prenderTodo.setOnClickListener{
            sendCommand("Y")
        }
    }

        //dddddddddddddddddddddddddddddddddddddd

        //eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
        private fun sendCommand(input: String) {
            if (m_bluetoothSocket != null) {
                try{
                    m_bluetoothSocket!!.outputStream.write(input.toByteArray())
                    //Toast.makeText(this, "ENCENDER", Toast.LENGTH_LONG).show()
                } catch(e: IOException) {
                    e.printStackTrace()
                }
            }
        }
}
