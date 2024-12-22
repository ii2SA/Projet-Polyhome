package com.itongisaaccharly.projet_household

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView

class DeviceAdapter(
    private val context: Context,
    private val datasource: ArrayList<DeviceData>
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.devices_list_item, parent, false)

        val deviceID = rowView.findViewById<TextView>(R.id.deviceID)
        val deviceType = rowView.findViewById<TextView>(R.id.deviceType)
        val deviceAvailableCommands = rowView.findViewById<Spinner>(R.id.availableCommandsSpinner)

        val device = datasource[position]

        deviceID.text = device.id
        deviceType.text = device.type

        // Préparer l'adaptateur du Spinner avec les commandes disponibles
        if (device.availableCommands != null && device.availableCommands.isNotEmpty()) {
            val spinnerAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                device.availableCommands
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            deviceAvailableCommands.adapter = spinnerAdapter
        }

        // Ajouter un listener pour envoyer la commande dès que l'utilisateur sélectionne une option
        deviceAvailableCommands.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCommand = parentView.getItemAtPosition(position).toString()

                if (selectedCommand.isNotEmpty()) {
                    // Appeler la méthode sendDeviceCommand avec l'ID de l'appareil et la commande
                    val loginToken = "votre_token_ici" // Remplacer par le token réel
                    val houseId = "votre_house_id" // Remplacer par l'ID de la maison réel

                    // L'appel de la méthode sendDeviceCommand dans DevicesActivity
                    if (context is DevicesActivity) {
                        val deviceId = device.id // L'ID de l'appareil
                        context.sendDeviceCommand(houseId, deviceId, selectedCommand, loginToken)
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Ne rien faire si aucune sélection n'est faite
            }
        }

        return rowView
    }

    override fun getItem(position: Int): Any {
        return datasource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return datasource.size
    }
}
