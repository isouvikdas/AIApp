package com.example.musicauthorfinder

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.musicauthorfinder.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {
    private lateinit var markwon: Markwon
    private var selectedImageURI: Uri? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var generativeModel: GenerativeModel
    private var imageBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_main)

        showToast("Use the app in light mode for better visibility")

        showEditButton()

        binding.addButton.visibility = View.VISIBLE


        generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = BuildConfig.apiKey
        )

        binding.postButton.setOnClickListener {
            if (isInternetAvailable()) {
                if (imageBitmap != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        generateContent()
                    }
                } else {
                    showToast("Please select an image first")
                }
            } else {
                showToast("No internet connection available")
            }
        }
        binding.apply {
            editButton.setOnClickListener {
                pickUpImage()
            }
            addButton.setOnClickListener {
                pickUpImage()
            }
        }

         markwon = Markwon.builder(this)
            .build()


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageURI = data?.data
             imageBitmap = selectedImageURI?.let {
                uriToBitmap(it)
            }
            binding.addButton.visibility = View.GONE
            binding.selectedImage.setImageBitmap(imageBitmap!!)
            showEditButton()

        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun pickUpImage() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .start()
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun generateContent() {
        try {
            withContext(Dispatchers.Main) {
                showLoading()
            }
                withContext(Dispatchers.IO) {
                    val inputContent = content {
                        image(imageBitmap!!)
                        text("Hey please suggest me some interior design for this")
                    }
                    val response = generativeModel.generateContent(inputContent)
                    print(response.text)
                    withContext(Dispatchers.Main) {
                        val formattedText = markwon.toMarkdown(response.text!!)
                        binding.suggestionText.text = formattedText
                        hideLoading()
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
            hideLoading()
        }
    }

    private fun showLoading() {
        binding.apply {
            postButton.isEnabled = false
            postButton.setText(R.string.loadingText)
            postButton.setTypeface(null, Typeface.BOLD)
        }
    }

    private fun hideLoading() {
        binding.apply {
            postButton.isEnabled = true
            postButton.setText(R.string.suggestionButtonText)
        }
    }
    private fun showEditButton() {
        binding.apply {
            if (selectedImageURI != null) {
                editButton.visibility = View.VISIBLE
            }
            else {
                editButton.visibility = View.GONE
            }
        }
    }
}