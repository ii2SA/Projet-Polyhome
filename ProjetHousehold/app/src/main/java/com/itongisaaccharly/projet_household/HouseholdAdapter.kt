package com.itongisaaccharly.projet_household

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class HouseholdAdapter (
        private val context: Context,
        private val datasource: ArrayList<HouseholdData>,
        private val loginToken: String,
        private var houseID : Int? = null
    ) : BaseAdapter()

{
        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rowView = inflater.inflate(R.layout.households_list_item, parent, false)
            val houseId = rowView.findViewById<TextView>(R.id.deviceID)
            val houseOwner = rowView.findViewById<TextView>(R.id.houseOwner)
            houseId.text = "Maison Numéro : ${datasource[position].houseId.toString()}"
            if(datasource[position].owner){
                houseOwner.text = "Proprietaire"
                houseID = datasource[position].houseId
            }
            else{ houseOwner.text = "Invité" }
            val goToHouseButton = rowView.findViewById<Button>(R.id.goToHouseButton)
            val removeAccessButton = rowView.findViewById<Button>(R.id.removeAccessButton)
            goToHouseButton.setOnClickListener {
                // Passer les données de la maison vers DevicesActivity
                val intent = Intent(context, DevicesActivity::class.java)
                val bundle = Bundle()
                bundle.putString("LoginToken", loginToken)  // Le LoginToken doit être récupéré du contexte (vous pouvez le stocker dans `HouseholdsActivity`)
                bundle.putString("houseID", datasource[position].houseId.toString())
                intent.putExtras(bundle)
                context.startActivity(intent)
            }
                return rowView
        }

        override fun getItem(position: Int): Any {
            val household = datasource[position]
            return household
        }

        override fun getItemId(position: Int): Long{
            val positionHousehold = position.toLong()
            return positionHousehold
        }
        override fun getCount(): Int {
            val sizeHouseholdsList = datasource.size
            return sizeHouseholdsList
        }
        fun getHouseID(): Int?{
            return houseID
        }

    }