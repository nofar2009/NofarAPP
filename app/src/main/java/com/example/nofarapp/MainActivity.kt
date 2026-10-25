package com.example.nofarapp

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var ivBase64Image: ImageView? = null
    private var selectedBitmap: Bitmap? = null
    private var firestoreImageDAO: FirestoreImageDAO? = null
    private var galleryImageLauncher: ActivityResultLauncher<String>? = null
    private val firestoreImageFileName = "my_profile_image"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivBase64Image = findViewById(R.id.ivBase64Image)
        val btnChooseGallery = findViewById<Button>(R.id.btnChooseGallery)
        val btnSaveFirestore = findViewById<Button>(R.id.btnSaveFirestore)
        val btnReadFirestore = findViewById<Button>(R.id.btnReadFirestore)

        firestoreImageDAO = FirestoreImageDAO()

        // Setup gallery launcher
        galleryImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(this, "לא נבחרה תמונה", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            try {
                val bitmap = ImageUtils.convertUriToBitmap(this, uri)
                if (bitmap == null) {
                    Toast.makeText(this, "שגיאה בטעינת התמונה", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                selectedBitmap = bitmap
                ivBase64Image?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                Toast.makeText(this, "שגיאה: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnChooseGallery?.setOnClickListener {
            galleryImageLauncher?.launch("image/*")
        }

        // Save image to Firestore as Base64
        btnSaveFirestore?.setOnClickListener {
            val bitmap = selectedBitmap
            if (bitmap == null) {
                Toast.makeText(this, "נא לבחור תמונה תחילה", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val base64String = ImageUtils.convertBitmapToBase64(bitmap)
            if (base64String == null) {
                Toast.makeText(this, "שגיאה בהמרת התמונה", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            firestoreImageDAO?.saveToFirestore(
                firestoreImageFileName,
                base64String,
                object : IFirestoreImageCallback<Void?> {
                    override fun onSuccess(result: Void?) {
                        Toast.makeText(this@MainActivity, "התמונה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(error: Exception) {
                        Toast.makeText(this@MainActivity, "השמירה נכשלה: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Read image from Firestore
        btnReadFirestore?.setOnClickListener {
            firestoreImageDAO?.readFromFirestore(
                firestoreImageFileName,
                object : IFirestoreImageCallback<String?> {
                    override fun onSuccess(base64: String?) {
                        val decodedBitmap = ImageUtils.convertBase64ToBitmap(base64)
                        if (decodedBitmap != null) {
                            ivBase64Image?.setImageBitmap(decodedBitmap)
                            Toast.makeText(this@MainActivity, "התמונה נטענה בהצלחה!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "שגיאה בפענוח הנתונים", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(error: Exception) {
                        Toast.makeText(this@MainActivity, "הקריאה נכשלה: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
