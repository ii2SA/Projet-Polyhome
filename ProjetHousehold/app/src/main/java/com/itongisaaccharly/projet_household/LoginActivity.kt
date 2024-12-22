package com.itongisaaccharly.projet_household

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        val btnConnect = findViewById<Button>(R.id.btnConect)
        btnConnect.setOnClickListener {
            Log.d("LoginActivity", "Button clicked")
            login()
        }
    }


    fun registerNewAccount(view: View) {
            val intent = Intent(this, RegisterActivity::class.java);
            startActivity(intent);
        }

    private fun loginSuccess(responseCode: Int, response: LoginResponse?) {
        val token : String? = response?.token
            when (responseCode) {
                200 -> {
                        runOnUiThread {
                            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_LONG).show()
                        }
                        Log.d("LoginActivity", "the loginToken is : $token")
                        val intent = Intent(this, HouseholdsActivity::class.java)
                        intent.putExtra("LoginToken", token)
                        startActivity(intent)

                }
                400 -> {
                    runOnUiThread{
                        Toast.makeText(this, "Les données fournies sont incorrectes", Toast.LENGTH_LONG).show()
                    }
                }

                404 -> {
                    runOnUiThread {
                        Toast.makeText(this, "Aucun utilisateur ne correspond aux identifiants donnés . Veuillez recommencez !", Toast.LENGTH_LONG).show()
                    }
                }

                else -> {
                    runOnUiThread {
                        Toast.makeText(this, "Une erreur s’est produite au niveau du serveur", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }


    public fun login() {
            val registerMail = findViewById<TextView>(R.id.txtMail).text.toString()
            val registerPassword = findViewById<TextView>(R.id.txtPassword).text.toString()
            val loginData = LoginData(registerMail, registerPassword)
            Api().post<LoginData, LoginResponse>("https://polyhome.lesmoulinsdudev.com/api/users/auth", loginData, ::loginSuccess)

    }

}