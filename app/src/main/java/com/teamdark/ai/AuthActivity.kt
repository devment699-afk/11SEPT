package com.teamdark.ai

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // already logged in -> go main
        if (UserManager.current(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_auth)

        val eName = findViewById<EditText>(R.id.aName)
        val eMail = findViewById<EditText>(R.id.aMail)
        val ePass = findViewById<EditText>(R.id.aPass)
        val btnReg = findViewById<TextView>(R.id.btnReg)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)

        btnReg.setOnClickListener {
            val r = UserManager.register(this, eName.text.toString(), eMail.text.toString(), ePass.text.toString())
            if (r == "OK") {
                Toast.makeText(this, "Welcome to TEAMDARK.AI", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else Toast.makeText(this, r, Toast.LENGTH_SHORT).show()
        }
        btnLogin.setOnClickListener {
            val r = UserManager.login(this, eMail.text.toString(), ePass.text.toString())
            if (r == "OK") {
                Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else Toast.makeText(this, r, Toast.LENGTH_SHORT).show()
        }
    }
}
