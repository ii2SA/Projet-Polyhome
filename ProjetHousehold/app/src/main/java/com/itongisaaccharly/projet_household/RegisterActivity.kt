package com.itongisaaccharly.projet_household

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            Log.d("RegisterActivity", "Button clicked")
            register()
        }
    }

    public fun goToLogin(view: View)
    {
        finish();
    }
    public fun register(){
        val registerMail = findViewById<TextView>(R.id.txtRegisterMail).text.toString()
        val registerPassword = findViewById<TextView>(R.id.txtRegisterPassword).text.toString()
        if (registerMail.isEmpty() || registerPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_LONG).show()
            return
        }
        val registerData = RegisterData(registerMail, registerPassword)
        Api().post<RegisterData>("https://polyhome.lesmoulinsdudev.com/api/users/register", registerData, ::registerSuccess)
    }
    private fun registerSuccess(responseCode: Int){
        Log.d("RegisterActivity", "Code de réponse API : $responseCode")
        runOnUiThread {
            when (responseCode){
                200 -> {
                    Toast.makeText(this, "Le compte a bien été créé", Toast.LENGTH_LONG).show()
                        finish()
                }
                400 -> {
                    runOnUiThread {
                        Toast.makeText(this, "Les données fournies sont incorrectes", Toast.LENGTH_LONG).show()
                    }
                }
                409 -> {
                    Toast.makeText(this, "Le login est déjà utilisé par un autre compte", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "Une erreur s’est produite au niveau du serveur", Toast.LENGTH_LONG).show()

                }
            }
        }
    }
}