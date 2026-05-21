package com.smartnotepad.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.smartnotepad.R
import com.smartnotepad.adapter.CountdownAdapter
import com.smartnotepad.data.DataStore
import com.smartnotepad.model.Countdown
import java.util.*

class CountdownFragment : Fragment() {

    private lateinit var dataStore: DataStore
    private lateinit var countdownAdapter: CountdownAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_countdown, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataStore = DataStore.getInstance(requireContext())

        setupCountdownList(view)
        setupFab(view)
    }

    override fun onResume() {
        super.onResume()
        refreshCountdowns()
    }

    private fun setupCountdownList(view: View) {
        val rvCountdowns = view.findViewById<RecyclerView>(R.id.rv_countdowns)
        rvCountdowns.layoutManager = LinearLayoutManager(requireContext())

        countdownAdapter = CountdownAdapter(
            countdowns = emptyList(),
            onItemLongClick = { countdown ->
                AlertDialog.Builder(requireContext())
                    .setTitle("删除倒计时")
                    .setMessage("确定要删除「${countdown.title}」吗？")
                    .setPositiveButton("确定") { _, _ ->
                        dataStore.deleteCountdown(countdown.id)
                        refreshCountdowns()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )
        rvCountdowns.adapter = countdownAdapter
    }

    private fun setupFab(view: View) {
        view.findViewById<FloatingActionButton>(R.id.fab_add_countdown).setOnClickListener {
            showAddCountdownDialog()
        }
    }

    private fun showAddCountdownDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_countdown, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_countdown_title)
        val etTargetDate = dialogView.findViewById<TextInputEditText>(R.id.et_target_date)

        etTargetDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    etTargetDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                etTitle.error = "请输入倒计时标题"
                return@setOnClickListener
            }
            val targetDate = etTargetDate.text.toString()
            if (targetDate.isEmpty()) {
                etTargetDate.error = "请选择目标日期"
                return@setOnClickListener
            }

            val countdown = Countdown(
                title = title,
                targetDate = targetDate
            )
            dataStore.addCountdown(countdown)
            refreshCountdowns()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun refreshCountdowns() {
        val countdowns = dataStore.getCountdowns()
        countdownAdapter.updateData(countdowns)
    }
}
