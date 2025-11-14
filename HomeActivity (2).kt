package com.ClassActivity1.geoquiz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ClassActivity1.geoquiz.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var csvUri: Uri? = null
    private val PICK_CSV_FILE = 1
    private val SETTINGS_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.importButton.setOnClickListener { openFile() }

        binding.startQuizButton.setOnClickListener {
            val prompt = binding.PromptEditText.text.toString().trim()
            if (prompt.isEmpty() && csvUri == null && MainActivity.questionBank.isEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("promptText", prompt)
                csvUri?.let { intent.putExtra("csvUri", it.toString()) }
                Toast.makeText(this, "Please upload a CSV or enter a prompt", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("promptText", prompt)
                csvUri?.let { intent.putExtra("csvUri", it.toString()) }
                startActivity(intent)
            }
        }

        val settingsButton: Button = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        startActivityForResult(intent, PICK_CSV_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                csvUri = uri
                loadQuestionsFromCsv(uri)
            }
        }

        if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            val questionTime = data?.getIntExtra("questionTime", 30) ?: 30
            val randomOrder = data?.getBooleanExtra("randomOrder", true) ?: true
            MainActivity.answerMs = questionTime * 1000L
            MainActivity.randomOrder = randomOrder
        }
    }

    private fun loadQuestionsFromCsv(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.bufferedReader()?.useLines { lines ->
                val questions = lines.map { line ->
                    val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                    if (parts.size < 6) throw Exception("CSV must have 6 columns: Question, A, B, C, D, Answer")
                    val text = parts[0]
                    val options = parts.subList(1, 5)
                    val answer = parts[5]
                    Question(text, answer, options)
                }.toMutableList()
                MainActivity.questionBank = questions
                Toast.makeText(this, "CSV imported successfully!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error reading CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}