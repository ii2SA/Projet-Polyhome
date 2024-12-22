package com.itongisaaccharly.projet_household

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DevicesActivity : AppCompatActivity() {
    private var devices: ArrayList<DeviceData> = ArrayList()  // Liste pour stocker les appareils
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.devices_activity)

        deviceAdapter = DeviceAdapter(this, devices)
        initializeDevicesList()
        loadDevices()

        val buttonStopAll = findViewById<Button>(R.id.stopAllDevicesButton)
        val buttonCloseAll = findViewById<Button>(R.id.closeAllDevicesButton)
        val buttonOpenAll = findViewById<Button>(R.id.openAllDevicesButton)
        buttonStopAll.setOnClickListener {
            controlAllDevices("STOP")
        }

        // Écouteur pour le bouton "Fermer tous les appareils"
        buttonCloseAll.setOnClickListener {
            controlAllDevices("CLOSE")
        }

        // Écouteur pour le bouton "Ouvrir tous les appareils"
        buttonOpenAll.setOnClickListener {
            controlAllDevices("OPEN")
        }
    }

    private fun initializeDevicesList() {
        val deviceListView = findViewById<ListView>(R.id.listDevices)
        deviceListView.adapter = deviceAdapter
    }

    private fun loadDevices() {
        val bundle = intent.extras
        if (bundle != null) {
            val loginToken = bundle.getString("LoginToken")
            val houseID = bundle.getString("houseID")
            if (loginToken != null && houseID != null) {
                val houseIDInt = houseID.toInt()
                Api().get("https://polyhome.lesmoulinsdudev.com/api/houses/$houseIDInt/devices", ::loadDeviceSuccess, loginToken)
            } else {
                Log.e("DeviceActivity", "LoginToken ou HouseID est nul.")
            }
        }
    }

    private fun loadDeviceSuccess(responseCode: Int, loadedDevices: DeviceHouses?) {
        runOnUiThread {
            if (responseCode == 200 && loadedDevices != null) {
                devices.clear()
                devices.addAll(loadedDevices.devices)
                deviceAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Erreur lors du chargement des appareils", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Correction de la méthode sendDeviceCommand
    fun sendDeviceCommand(houseId: String, deviceId: String, command: String, token: String) {
        val spinnerCommand = findViewById<Spinner>(R.id.availableCommandsSpinner)
        val selectedCommand = spinnerCommand.selectedItem as? String
        if (selectedCommand != null) {
            val commandSended = CommandData(selectedCommand)

            val bundle = intent.extras
            if (bundle != null) {
                val loginToken = bundle.getString("LoginToken")
                val houseID = bundle.getString("houseID")
                if (loginToken != null && houseID != null) {
                    Log.d("DeviceActivity", "LoginToken: $loginToken, HouseID: $houseID")
                    val houseIDInt = houseID.toInt()

                    // Nous utilisons deviceId dans l'URL pour identifier l'appareil.
                    Api().post(
                        "https://polyhome.lesmoulinsdudev.com/api/houses/$houseIDInt/devices/$deviceId/command",
                        commandSended,
                        ::sendCommandSuccess,
                        loginToken
                    )
                } else {
                    Log.e("DeviceActivity", "LoginToken ou HouseID est nul.")
                }
            } else {
                Log.e("DeviceActivity", "Les extras de l'Intent sont nuls.")
            }
        }
    }

    // Méthode de succès de l'envoi de la commande
    private fun sendCommandSuccess(responseCode: Int) {
        runOnUiThread {
            when (responseCode) {
                200 -> Toast.makeText(this, "Commande envoyée avec succès!", Toast.LENGTH_SHORT).show()
                403 -> Toast.makeText(this, "Accès interdit. Vérifiez votre token.", Toast.LENGTH_SHORT).show()
                500 -> Toast.makeText(this, "Erreur serveur.", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "Erreur inconnue. Code $responseCode", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun controlAllDevices(command: String) {
        val bundle = intent.extras
        if (bundle != null) {
            val loginToken = bundle.getString("LoginToken")
            val houseID = bundle.getString("houseID")
            if (loginToken != null && houseID != null) {
                val houseIDInt = houseID.toInt()

                // Envoyer la commande à tous les appareils
                devices.forEach { device ->
                    val deviceId = device.id
                    // Vérifier si l'appareil est de type "light"
                    if (device.type == "light") {
                        // Commande pour arrêter ou fermer les lumières
                        if (command == "stop" || command == "close") {
                            sendDeviceCommand(houseIDInt.toString(), deviceId, "TURN OFF", loginToken)
                        } else if (command == "open") {
                            sendDeviceCommand(houseIDInt.toString(), deviceId, "TURN ON", loginToken)
                        }
                    } else {
                        // Envoyer la commande pour d'autres types d'appareils (par exemple "close", "open", "stop")
                        sendDeviceCommand(houseIDInt.toString(), deviceId, command, loginToken)
                    }
                }
            }
        }
    }

}
