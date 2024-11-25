
package com.example.ekgappab

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.ekgappab.bluetoothHandler.BleDataReceiver
import com.example.ekgappab.bluetoothHandler.DeviceAction
import java.util.UUID


class ShowResultFragment : Fragment() {

    private lateinit var dataResultDB: DataResultDB
    private lateinit var heartRateTextView: TextView
    private lateinit var heartRateTextViewEcg: TextView

    private lateinit var bpSysDiaTextView: TextView

    private lateinit var bpUsageImg: ImageView
    private lateinit var ecgUsageImg: ImageView
    private lateinit var saveBPData: Button
    private lateinit var saveEcgData: Button

    private lateinit var containerHRecg: LinearLayout
    private lateinit var bloodPressureTab: TextView
    private lateinit var ecgTab: TextView
    private lateinit var bpDataView: View
    private lateinit var ecgDataView: View

    private lateinit var bleDataReceiver: BleDataReceiver
    private var isReceiverRegistered = false


    private var finalHeartRate: Int = 0
    private var finalBpSys: Int = 0
    private var finalBpDia: Int = 0

    private var finalEcgDataList: MutableList<Float> = mutableListOf()

    private lateinit var ecgGraphView: EcgGraphView
    private lateinit var ecgGraphView1: EcgGraphView
    private lateinit var ecgGraphView2: EcgGraphView
    private lateinit var ecgGraphView3: EcgGraphView



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_result, container, false)
        dataResultDB = DataResultDB()
        // Ініціалізуємо елементи UI
        bloodPressureTab = view.findViewById(R.id.bloodPressureTab)
        ecgTab = view.findViewById(R.id.ecgTab)
        bpDataView = view.findViewById(R.id.bpDataView)
        ecgDataView = view.findViewById(R.id.ecgDataView)

        heartRateTextView = view.findViewById(R.id.heartRateTextView)
        heartRateTextViewEcg = view.findViewById(R.id.heartRateTextViewEcg)
        bpSysDiaTextView = view.findViewById(R.id.bpSysDiaTextView)


        bpUsageImg = view.findViewById(R.id.bpUsageImg)
        ecgUsageImg = view.findViewById(R.id.ecgUsageImg)

        saveBPData = view.findViewById(R.id.saveBPButton)
        saveEcgData = view.findViewById(R.id.saveEcgButton)

        containerHRecg = view.findViewById(R.id.middleContEcg)
        bpDataView.visibility = View.VISIBLE
        ecgDataView.visibility = View.GONE
        bloodPressureTab.setTextColor(resources.getColor(R.color.activeTabColor))
        bloodPressureTab.setTypeface(null, Typeface.BOLD)

        ecgGraphView = view.findViewById(R.id.ecgGraphView)
        ecgGraphView1 = view.findViewById(R.id.ecgGraphView)
        ecgGraphView2 = view.findViewById(R.id.ecgGraphView)
        ecgGraphView3 = view.findViewById(R.id.ecgGraphView)
        // Додамо обробники натискання для табів
        bloodPressureTab.setOnClickListener {
            showBloodPressureView()
        }
        ecgTab.setOnClickListener {
            showEcgView()
        }

        saveBPData.setOnClickListener {
            val sessionManager = UserSessionManager(requireContext())
            val userId = sessionManager.getUserId()?: ""
            val bpId = UUID.randomUUID().toString().take(20)
            val bpData = BPData(
                bpId = bpId,
                userId = userId,
                heartrate = finalHeartRate,
                bpsys = finalBpSys,
                bpdia = finalBpDia,
                date = dataResultDB.getCurrentTimestamp(),
                time = dataResultDB.getCurrentTimestamp()
            )
            dataResultDB.saveBPData(bpData)
            Toast.makeText(context, "Дані успішно збережено!", Toast.LENGTH_SHORT).show()
        }
        saveEcgData.setOnClickListener {
            val sessionManager = UserSessionManager(requireContext())
            val userId = sessionManager.getUserId()?: ""
            val ecgId = UUID.randomUUID().toString().take(20)
            val ecgData = EcgData(
                ecgId = ecgId,
                userId = userId,
                heartrate = finalHeartRate,
                ecgDataArray = finalEcgDataList,
                date = dataResultDB.getCurrentTimestamp(),
                time = dataResultDB.getCurrentTimestamp()
            )
            dataResultDB.saveEcgData(ecgData)
            Toast.makeText(context, "Дані успішно збережено!", Toast.LENGTH_SHORT).show()
        }


        // Підписуємось на зміни LiveData з BleDataReceiver
        BleDataReceiver.heartRateLive.observe(viewLifecycleOwner) { rate ->
            if (rate != 0) {
                finalHeartRate = rate
                updateBpAndHeartRateView()
                updateECGView()
            }
        }

        BleDataReceiver.bpSysLive.observe(viewLifecycleOwner) { sys ->
                sys?.let { finalBpSys = it }
                updateBpAndHeartRateView()
        }

        BleDataReceiver.bpDiaLive.observe(viewLifecycleOwner) { dia ->
                dia?.let { finalBpDia = it }
                updateBpAndHeartRateView()
        }

        BleDataReceiver.ecgDataLive.observe(viewLifecycleOwner) { wave ->
            if (wave != null && wave.isNotEmpty()) {
                if (finalEcgDataList.size > 125 * 30) {

                    finalEcgDataList.clear()
                    finalHeartRate = 0
                }
                // Додаємо нові дані до загального списку
                finalEcgDataList.addAll(wave.toList())
                ecgGraphView.updateData(finalEcgDataList.toFloatArray())
                updateECGView()

            }
        }


        return view
    }



    private fun showBloodPressureView() {
        // Активуємо видимість контейнера Blood Pressure
        bpDataView.visibility = View.VISIBLE
        ecgDataView.visibility = View.GONE

        // Змінюємо стиль активної вкладки
        bloodPressureTab.setTextColor(resources.getColor(R.color.activeTabColor))
        bloodPressureTab.setTypeface(null, Typeface.BOLD)

        ecgTab.setTextColor(resources.getColor(R.color.inactiveTabColor))
        ecgTab.setTypeface(null, Typeface.NORMAL)

    }



    private fun showEcgView() {
        // Активуємо видимість контейнера ECG
        ecgDataView.visibility = View.VISIBLE
        bpDataView.visibility = View.GONE

        // Змінюємо стиль активної вкладки
        ecgTab.setTextColor(resources.getColor(R.color.activeTabColor))
        ecgTab.setTypeface(null, Typeface.BOLD)

        bloodPressureTab.setTextColor(resources.getColor(R.color.inactiveTabColor))
        bloodPressureTab.setTypeface(null, Typeface.NORMAL)
    }

    private fun updateBpAndHeartRateView() {
        if (finalBpSys != 0 && finalBpDia != 0 && finalHeartRate != 0) {
            bpUsageImg.visibility = View.GONE
            bpSysDiaTextView.text = "$finalBpSys / $finalBpDia mmHg"
            heartRateTextView.text = "Heart Rate: $finalHeartRate"
            saveBPData.visibility = View.VISIBLE
        }
        else{
            bpUsageImg.visibility = View.VISIBLE
            bpSysDiaTextView.text = "-- / -- mmHg"
            heartRateTextView.text = "Heart Rate: --"
            saveBPData.visibility = View.GONE
        }
    }

    private fun updateECGView() {
        if (finalEcgDataList.isNotEmpty()){
            ecgUsageImg.visibility = View.GONE
            containerHRecg.visibility  = View.VISIBLE
            heartRateTextViewEcg.text = "Heart Rate: $finalHeartRate"
            ecgGraphView.visibility = View.VISIBLE
            saveEcgData.visibility = View.VISIBLE

        }
        else{
            ecgUsageImg.visibility = View.VISIBLE
            containerHRecg.visibility = View.GONE
            saveEcgData.visibility = View.GONE
        }
    }


    override fun onStart() {
        super.onStart()

        // Створюємо та реєструємо BroadcastReceiver для отримання даних
        bleDataReceiver = BleDataReceiver() // ініціалізація
        val filter = IntentFilter().apply {
            addAction(DeviceAction.ACTION_HEART_DATA_AVAILABLE)
            addAction(DeviceAction.ACTION_BP2_DATA_AVAILABLE)
        }
        if (!isReceiverRegistered) {
            requireContext().registerReceiver(bleDataReceiver, filter) // використання bleDataReceiver
            isReceiverRegistered = true
        }
    }

    override fun onStop() {
        super.onStop()
        // Відміняємо реєстрацію BroadcastReceiver
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(bleDataReceiver) // використання bleDataReceiver
            isReceiverRegistered = false
        }
    }
}