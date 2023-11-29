package com.example.camerax

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmap = _bitmaps.asStateFlow()

    fun onTakePhoto(bitmap: Bitmap, context: Context) {
        viewModelScope.launch {
            saveImageToStorage(context, bitmap)
        }
        _bitmaps.value += bitmap
    }

    fun getImages(context: Context): List<Bitmap> {
        val images = mutableListOf<Bitmap>()
        readImagesFromFolder(context, images)
        return images
    }

    private fun readImagesFromFolder(
        context: Context,
        images: MutableList<Bitmap>
    ) {
        val cr = context.contentResolver

        val projection = arrayOf(MediaStore.MediaColumns._ID)

        val cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (it.moveToNext()) {
                val imageId = it.getLong(idColumn)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)

                val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(cr, imageUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(cr, imageUri)
                }
                images.add(bitmap)
            }
        }
    }

    private fun saveImageToStorage(context: Context, bitmap: Bitmap) {
        val cr = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "CameraX ${getCurrentDateTimeWithTimeZone()}")
            put(MediaStore.Images.Media.MIME_TYPE, Constants.mimeTypeForImage)
            put(MediaStore.Images.Media.RELATIVE_PATH, Constants.dirToSaveMedia)
        }
        val uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            val outputStream = cr.openOutputStream(uri)
            outputStream?.let { it1 -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it1) }
            outputStream?.close()
        }
    }
}