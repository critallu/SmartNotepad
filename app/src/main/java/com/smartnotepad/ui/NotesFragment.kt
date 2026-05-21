package com.smartnotepad.ui

import android.app.AlertDialog
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
import com.smartnotepad.adapter.NoteAdapter
import com.smartnotepad.data.DataStore
import com.smartnotepad.model.Note

class NotesFragment : Fragment() {

    private lateinit var dataStore: DataStore
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataStore = DataStore.getInstance(requireContext())

        setupNoteList(view)
        setupFab(view)
    }

    override fun onResume() {
        super.onResume()
        refreshNotes()
    }

    private fun setupNoteList(view: View) {
        val rvNotes = view.findViewById<RecyclerView>(R.id.rv_notes)
        rvNotes.layoutManager = LinearLayoutManager(requireContext())

        noteAdapter = NoteAdapter(
            notes = emptyList(),
            onItemClick = { note ->
                showEditNoteDialog(note)
            },
            onItemLongClick = { note ->
                AlertDialog.Builder(requireContext())
                    .setTitle("删除笔记")
                    .setMessage("确定要删除「${note.title.ifEmpty { "无标题" }}」吗？")
                    .setPositiveButton("确定") { _, _ ->
                        dataStore.deleteNote(note.id)
                        refreshNotes()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )
        rvNotes.adapter = noteAdapter
    }

    private fun setupFab(view: View) {
        view.findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_note, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_note_title)
        val etContent = dialogView.findViewById<TextInputEditText>(R.id.et_note_content)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() && content.isEmpty()) {
                etTitle.error = "请输入标题或内容"
                return@setOnClickListener
            }

            val note = Note(
                title = title,
                content = content
            )
            dataStore.addNote(note)
            refreshNotes()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditNoteDialog(note: Note) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_note, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_note_title)
        val etContent = dialogView.findViewById<TextInputEditText>(R.id.et_note_content)

        etTitle.setText(note.title)
        etContent.setText(note.content)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            val updatedNote = note.copy(
                title = title,
                content = content,
                updateTime = System.currentTimeMillis()
            )
            dataStore.updateNote(updatedNote)
            refreshNotes()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun refreshNotes() {
        val notes = dataStore.getNotes()
        noteAdapter.updateData(notes)
    }
}
