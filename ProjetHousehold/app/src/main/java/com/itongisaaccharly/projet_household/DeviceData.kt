package com.itongisaaccharly.projet_household

data class DeviceData(
    val id : String,
    val type : String,
    val availableCommands : List<String>,
    val opening : Number?,
    val power : Number?
){

}
