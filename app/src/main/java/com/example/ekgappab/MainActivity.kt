package com.example.ekgappab

import android.Manifest

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.Observer
import com.example.ekgappab.bluetoothHandler.BLEManager
import com.example.ekgappab.bluetoothHandler.ScanListener
import com.example.ekgappab.bluetoothHandler.StateListener

class MainActivity : AppCompatActivity(), ScanListener, StateListener {


    private lateinit var userSessionManager: UserSessionManager
    private val userViewModel: UserViewModelClass by viewModels()

    private lateinit var logTextView: TextView
    private lateinit var deviceContainer: LinearLayout
    private lateinit var logContainer: LinearLayout
    private lateinit var deviceNameTextView: TextView
    private lateinit var searchButton: ImageButton
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bleManager: BLEManager

    private var isDeviceConnected: Boolean = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 2

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ініціалізуємо менеджер сесій користувача
        userSessionManager = UserSessionManager(this)
        val userId = userSessionManager.getUserId()



        // Ініціалізуємо елементи інтерфейсу
        logTextView = findViewById(R.id.log_text_view)
        deviceContainer = findViewById(R.id.device_container)
        logContainer = findViewById(R.id.log_container)
        deviceNameTextView = findViewById(R.id.device_name)
        searchButton = findViewById(R.id.search_button)
        fragmentContainer = findViewById(R.id.fragment_container)
        bleManager = BLEManager(this, this, this)

        val userNameTextView = findViewById<TextView>(R.id.userName)
        val userEmailTextView = findViewById<TextView>(R.id.userEmail)

        // Завантаження даних користувача з ViewModel, якщо є userId
        if (userId != null) {
            userViewModel.loadUserData(userId)
        }

        userViewModel.getUserData().observe(this, Observer { userData ->
            if (userData != null) {
                val fullName = "${userData["firstName"]} ${userData["surname"]}"
                val email = userData["email"] ?: "email do not exist"
                userNameTextView.text = fullName
                userEmailTextView.text = email
            } else {
                userNameTextView.text = "No name"
                userEmailTextView.text = "email@example.com"
            }
        })

        // Дії кнопки пошуку
        searchButton.setOnClickListener {
            logTextView.text = ""
            checkBluetoothPermissions()
        }

        // Налаштування нижньої навігації
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> loadShowResultFragment()
                R.id.nav_history -> HistoryFragment()
                R.id.nav_settings -> SettingFragment()
                else -> loadShowResultFragment() // Завантажується як основний фрагмент
            }
            loadFragment(fragment)
            true
        }

        // Завантаження початкового фрагменту
        if (savedInstanceState == null) {
            loadFragment(loadShowResultFragment())
        }
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun loadShowResultFragment(): Fragment {
        return if (isDeviceConnected) {
          ShowResultFragment()
        } else {
          DeviceNotConnectedFragment()
        }
    }


    private fun checkBluetoothPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val requiredPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (requiredPermissions.isNotEmpty()) {
            requestPermissions(requiredPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startBLEScan()
        }
    }

    private fun startBLEScan() {
        log("Starting the device BP2 search ...")
        try {
            bleManager.startScan()
        } catch (e: Exception) {
            log("Error when starting the scan: ${e.localizedMessage}")
        }
    }

    override fun onScanStarted() {
        logTextView.text = ""
        log("The scan has started...")
    }

    override fun onScanStopped() {
        logTextView.text = ""
        log("The scan is finished.")
    }

    override fun onConnected() {
        runOnUiThread {
            logTextView.text = ""
            logContainer.visibility = View.GONE
            deviceNameTextView.text = "BP2"
            deviceContainer.visibility = View.VISIBLE
            isDeviceConnected = true

        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            logContainer.visibility = View.VISIBLE
            logTextView.text = ""
            log("The connection is lost, please check whether the device or bluetooth is turned on.")
            deviceContainer.visibility = View.GONE
            searchButton.visibility = View.VISIBLE

            isDeviceConnected = false
        }
    }



    private fun log(message: String) {
        runOnUiThread {
            logTextView.append("$message\n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect()
    }
}
