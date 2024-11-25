package com.example.ekgappab

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Налаштування Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)
        toolbar.setNavigationOnClickListener { finish() }

        // Ініціалізація Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Знаходимо елементи інтерфейсу
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val errorTextView = findViewById<TextView>(R.id.error_text_view)

        // Обробник натискання кнопки "Увійти"
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                errorTextView.text = "Будь ласка, заповніть усі поля"
                errorTextView.visibility = TextView.VISIBLE
            } else {
                errorTextView.visibility = TextView.GONE

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                saveUserSession(userId) // Зберігаємо сесію
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish() // Закриваємо LoginActivity
                            }
                        } else {
                            showError("Помилка входу: ${task.exception?.message}")
                        }
                    }
            }
        }
    }

    // Збереження userId в SharedPreferences
    private fun saveUserSession(userId: String) {
        val sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("userId", userId)
            apply()
        }
    }

    private fun showError(message: String) {
        val errorTextView = findViewById<TextView>(R.id.error_text_view)
        errorTextView.visibility = TextView.GONE
        errorTextView.text = message
        errorTextView.visibility = TextView.VISIBLE
    }
}
