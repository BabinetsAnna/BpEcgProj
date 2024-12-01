package com.example.ekgappab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ShowEcgHistoryFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var ecgGraphView: EcgGraphView

    companion object {
        private const val ARG_ECG_ID = "ecg_id"

        fun newInstance(ecgId: String): ShowEcgHistoryFragment {
            val fragment = ShowEcgHistoryFragment()
            val args = Bundle()
            args.putString(ARG_ECG_ID, ecgId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_ecg_history, container, false)

        val backButton = view.findViewById<Button>(R.id.btn_back)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val heartRateTextView = view.findViewById<TextView>(R.id.heartRateTextView)
        val ecgGraphView1 = view.findViewById<EcgGraphView>(R.id.ecgGraphView1)
        val ecgGraphView2 = view.findViewById<EcgGraphView>(R.id.ecgGraphView2)
        val ecgGraphView3 = view.findViewById<EcgGraphView>(R.id.ecgGraphView3)

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val ecgId = arguments?.getString(ARG_ECG_ID)
        if (ecgId.isNullOrEmpty()) {
            Toast.makeText(context, "Не вказано ID вимірювання", Toast.LENGTH_SHORT).show()
            return view
        }

        database = FirebaseDatabase.getInstance().getReference("EcgData")

        database.orderByChild("ecgId").equalTo(ecgId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val ecgData = snapshot.children.firstOrNull()?.getValue(EcgData::class.java)
                    if (ecgData != null) {
                        val formattedDate = DataResultDB().convertTimestampToDate(ecgData.date)
                        dateTextView.text = " $formattedDate"
                        heartRateTextView.text = "HR: ${ecgData.heartrate}"

                        val ecgDataArray =
                            ecgData.ecgDataArray.toFloatArray() // Перетворюємо на FloatArray

                        val partSize = ecgDataArray.size / 3

                        val part1 = ecgDataArray.copyOfRange(
                            0,
                            partSize
                        ) // Використовуємо copyOfRange для масивів
                        val part2 = ecgDataArray.copyOfRange(partSize, 2 * partSize)
                        val part3 = ecgDataArray.copyOfRange(2 * partSize, ecgDataArray.size)

                        // Оновлення графіків
                        ecgGraphView1.updateData(part1)
                        ecgGraphView2.updateData(part2)
                        ecgGraphView3.updateData(part3)

                    } else {
                        Toast.makeText(context, "Дані не знайдені", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Дані не знайдені", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Помилка завантаження даних: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        return view
    }
}

