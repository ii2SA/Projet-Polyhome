package com.itongisaaccharly.projet_household

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast


class HouseholdsActivity : AppCompatActivity() {
    private var households: ArrayList<HouseholdData> = ArrayList()
    private var users: ArrayList<String> = ArrayList()
    private lateinit var userAdapter: ArrayAdapter<String>
    private lateinit var adapter: HouseholdAdapter
    private var selectedHousehold: HouseholdData? = null
    private var loginToken: String = ""
    private var houseID: Int? = 0
    private var userAccessList: ArrayList<UserData> = ArrayList()  // Liste des utilisateurs avec leurs houseId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.households_activity)
        loginToken = intent.getStringExtra("LoginToken") ?: ""
        adapter = HouseholdAdapter(this, households, loginToken)
        userAdapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, users)
        initializeHouseholdsList()
        initializeUsersSpinner()
        loadHouseholds()
        loadUsersSpinner()
    }

    public fun goToHousehold(view: View) {
        val household = selectedHousehold
        if (household != null) {
            if (loginToken.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("LoginToken", loginToken)
                    putString("houseID", household.houseId.toString())
                }

                val deviceIntent = Intent(this, DevicesActivity::class.java).apply {
                    putExtras(bundle)
                }

                Log.d("HouseholdsActivity", "Login Token : $loginToken")
                Log.d("HouseholdsActivity", "House ID: ${household.houseId}")
                startActivity(deviceIntent)
            } else {
                Log.e("HouseholdsActivity", "LoginToken est nul")
            }
        } else {
            Toast.makeText(this, "Veuillez sélectionner une maison d'abord.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeHouseholdsList() {
        val householdsListView = findViewById<ListView>(R.id.lstHouseholds)
        householdsListView.adapter = adapter

        householdsListView.setOnItemClickListener { _, _, position, _ ->
            selectedHousehold = households[position]  // On stocke la maison sélectionnée
            Log.d("HouseholdsActivity", "Maison sélectionnée: ${selectedHousehold?.houseId}")
        }
    }

    private fun loadHouseholdsSuccess(responseCode: Int, loadedHouseholds: List<HouseholdData>?) {
        runOnUiThread {
            if (responseCode == 200 && loadedHouseholds != null) {
                Toast.makeText(this, "Chargement de la liste des maisons réussi !", Toast.LENGTH_SHORT).show()
                households.clear()
                households.addAll(loadedHouseholds)
                updateHouseholdsList()
                if (households.isNotEmpty()) {
                    houseID = households[0].houseId
                }
            } else if (responseCode == 400) {
                Toast.makeText(this, "Les données fournies sont incorrectes !", Toast.LENGTH_SHORT).show()
            } else if (responseCode == 403) {
                Toast.makeText(this, "Accès interdit (token invalide) !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Une erreur s'est produite au niveau du serveur !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHouseholds() {
        if (loginToken.isNotEmpty()) {
            Api().get("https://polyhome.lesmoulinsdudev.com/api/houses", ::loadHouseholdsSuccess, loginToken)
        } else {
            Log.e("HouseholdsActivity", "LoginToken est vide")
            Toast.makeText(this, "Erreur : Token de connexion manquant", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHouseholdsList() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    private fun initializeUsersSpinner() {
        val spinUsersList = findViewById<Spinner>(R.id.usersListSpinner)
        spinUsersList.adapter = userAdapter
    }

    private fun loadUsersSpinner() {
        Api().get("https://polyhome.lesmoulinsdudev.com/api/users", ::loadUsersSpinnerSuccess)
    }

    private fun loadUsersSpinnerSuccess(responseCode: Int, loadedUsers: List<UserData>?) {
        runOnUiThread {
            if (responseCode == 200 && loadedUsers != null) {
                Toast.makeText(this, "Chargement des comptes créés !", Toast.LENGTH_SHORT).show()
                users.clear()
                for (user in loadedUsers) {
                    users.add(user.login)
                    // Ajout de l'utilisateur avec son userHouseId
                    userAccessList.add(UserData(user.login, user.login, user.houseId)) // stockez le houseId de l'utilisateur
                }
                updateUsersList()
            } else {
                Toast.makeText(this, "Erreur au niveau du serveur !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUsersList() {
        runOnUiThread {
            userAdapter.notifyDataSetChanged()
        }
    }

    private fun giveAccessToHouseholdSuccess(responseCode: Int) {
        runOnUiThread {
            when (responseCode) {
                200 -> {
                    Toast.makeText(this, "Accès accordé!", Toast.LENGTH_SHORT).show()
                    loadHouseholds()
                }
                400 -> {
                    Toast.makeText(this, "Les données fournies sont incorrectes.", Toast.LENGTH_SHORT).show()
                }
                403 -> {
                    Toast.makeText(this, "Accès interdit (token invalide ou ne correspondant pas au propriétaire de la maison).", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Une Erreur s'est produite : code $responseCode !", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun giveAccessToHousehold(view: View) {
        val spinUser = findViewById<Spinner>(R.id.usersListSpinner)
        val selectedUser = spinUser.selectedItem as? String
        if (selectedUser != null) {
            val userData = UserData(selectedUser, selectedUser)
            // Ajout du UserData avec son houseId (assurez-vous de le définir correctement)
            val userHouseId = houseID ?: 0 // Utilisez la valeur de houseID ici
            userAccessList.add(UserData(selectedUser, selectedUser, userHouseId))
            Api().post<UserData>("https://polyhome.lesmoulinsdudev.com/api/houses/$houseID/users", userData, ::giveAccessToHouseholdSuccess, loginToken)
        } else {
            Toast.makeText(this, "Veuillez sélectionner un utilisateur s'il vous plaît !", Toast.LENGTH_SHORT).show()
        }
    }

    public fun removeAccessFromHousehold(view: View) {
        val household = selectedHousehold
        if (household != null) {
            // Trouver l'utilisateur dans la liste userAccessList dont le userHouseId correspond au houseId de la maison sélectionnée
            val userToRemove = userAccessList.find { it.houseId == household.houseId }
            if (userToRemove != null) {
                val removedUser = userToRemove
                val apiUrl = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseID/users/"
                Api().delete(apiUrl, removedUser, ::onRemoveAccessSuccess, loginToken)
            } else {
                runOnUiThread {
                Toast.makeText(this, "Aucun utilisateur trouvé avec ce houseId.", Toast.LENGTH_SHORT).show()
                }}
        } else {
            runOnUiThread {
            Toast.makeText(this, "Aucune maison sélectionnée.", Toast.LENGTH_SHORT).show()
            }}
    }

    private fun onRemoveAccessSuccess(responseCode: Int) {
        runOnUiThread {
            if (responseCode == 200) {
                Toast.makeText(this, "Accès retiré avec succès!", Toast.LENGTH_SHORT).show()
                loadHouseholds()  // Recharger les maisons après suppression de l'accès
            } else if (responseCode == 400) {
                Toast.makeText(this, "Données fournies incorrectes", Toast.LENGTH_SHORT).show()
            } else if (responseCode == 403) {
                Toast.makeText(this, "Accès interdit (token invalide)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Une erreur s'est produite au niveau du serveur", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

