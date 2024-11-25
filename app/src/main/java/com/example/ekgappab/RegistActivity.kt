package com.example.ekgappab

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistActivity : AppCompatActivity() {

    // Створюємо посилання на Firebase Auth та Realtime Database
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var errorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Ініціалізація Firebase Auth та Realtime Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://ekgappab-5eef0-default-rtdb.firebaseio.com/").reference

        // Налаштування Toolbar з кнопкою "назад"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)
        toolbar.setNavigationOnClickListener {
            // Повернення на попередній екран
            finish()
        }

        // Знаходимо елементи інтерфейсу
        val surnameInput = findViewById<EditText>(R.id.surname_input)
        val firstNameInput = findViewById<EditText>(R.id.first_name_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val genderSpinner = findViewById<Spinner>(R.id.gender_spinner)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_password_input)
        val registerButton = findViewById<Button>(R.id.register_button)
        errorTextView = findViewById(R.id.error_text_view)

        // Обробник кнопки реєстрації
        registerButton.setOnClickListener {
            val surname = surnameInput.text.toString().trim()
            val firstName = firstNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            val gender = genderSpinner.selectedItem.toString()


            // Валідація даних
            when {
                surname.isEmpty() || firstName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    displayError("Всі поля обов'язкові")
                }
                password.length < 8 -> {
                    displayError("Пароль повинен містити не менше 8 символів")
                }
                password != confirmPassword -> {
                    displayError("Паролі не збігаються")
                }
                else -> {
                    // Реєстрація користувача у Firebase Auth
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Отримуємо унікальний ID користувача
                                val userId = auth.currentUser?.uid

                                // Створюємо об'єкт користувача
                                val user = User(surname, firstName, email, gender)

                                // Збереження даних користувача в Realtime Database
                                userId?.let {
                                    database.child("users").child(it).setValue(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Реєстрація успішна", Toast.LENGTH_SHORT).show()
                                            // Переадресація на LoginActivity
                                            startActivity(Intent(this, LoginActivity::class.java))
                                            finish() // Закриваємо поточну активність
                                        }
                                        .addOnFailureListener {
                                            displayError("Помилка збереження даних")
                                        }
                                }
                            } else {
                                displayError("Помилка реєстрації: ${task.exception?.message}")
                            }
                        }
                }
            }
        }
    }

    // Функція для відображення помилок
    private fun displayError(message: String) {
        errorTextView.visibility = TextView.GONE
        errorTextView.text = message
        errorTextView.visibility = TextView.VISIBLE
    }
}
