package com.sevenmillion.camerasave

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var openBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var nameInput: EditText

    private var shot: Bitmap? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
            if (bmp != null) {
                shot = bmp
                nameInput.visibility = View.VISIBLE
                saveBtn.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openBtn = findViewById(R.id.openCamera)
        saveBtn = findViewById(R.id.saveBtn)
        nameInput = findViewById(R.id.fileName)

        openBtn.setOnClickListener { cameraLauncher.launch() }

        saveBtn.setOnClickListener {
            val filename = nameInput.text.toString().trim()
            if (filename.isEmpty() || shot == null) {
                Toast.makeText(this, "Take picture & set name first", Toast.LENGTH_SHORT).show()
            } else {
                saveImageToFolder(shot!!, filename)
            }
        }
    }

    private fun saveImageToFolder(bitmap: Bitmap, filename: String) {
        val relativeLocation = "Android/data/7M APP/Pictures"
        val resolver = contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
        }

        val uri: Uri? =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    throw java.io.IOException("Failed to save bitmap.")
                }
            }
            Toast.makeText(this, "Saved to $relativeLocation/$filename.jpg", Toast.LENGTH_LONG).show()
            nameInput.text.clear()
            nameInput.visibility = View.GONE
            saveBtn.visibility = View.GONE
        } ?: run {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
        }
    }
}
