/*
 * Copyright 2020 Clifford Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.madness.collision.unit.image_modifying

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.heifwriter.HeifWriter
import androidx.lifecycle.observe
import com.madness.collision.R
import com.madness.collision.settings.SettingsFunc
import com.madness.collision.unit.Unit
import com.madness.collision.util.*
import kotlinx.android.synthetic.main.unit_im.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import com.madness.collision.unit.image_modifying.R as MyR

class MyUnit: Unit(){
    companion object {
        const val REQUEST_GET_IMAGE = 100
        const val permission_REQUEST_EXTERNAL_STORAGE = 200
    }

    private var image: Bitmap? = null
    private var imageName: String = ""

    override fun createOptions(context: Context, toolbar: Toolbar, iconColor: Int): Boolean {
        toolbar.setTitle(R.string.developertools_cropimage)
        toolbar.inflateMenu(MyR.menu.toolbar_im)
        toolbar.menu.findItem(MyR.id.imToolbarDone).icon.setTint(ThemeUtil.getColor(context, R.attr.colorActionPass))
        return true
    }

    override fun selectOption(item: MenuItem): Boolean {
        when (item.itemId){
            MyR.id.imToolbarDone -> {
                val context = context ?: return false
                image ?: return false
                actionDone(context)
                return true
            }
        }
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context
        if (context != null) SettingsFunc.updateLanguage(context)
        return inflater.inflate(MyR.layout.unit_im, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = context ?: return
        democratize()
        mainViewModel.contentWidthTop.observe(viewLifecycleOwner) {
            imageContainer?.alterPadding(top = it)
        }
        imagePreview.setOnClickListener{
            val getImage = Intent(Intent.ACTION_GET_CONTENT)
            getImage.type = "image/*"
            startActivityForResult(getImage, REQUEST_GET_IMAGE)
        }
        val tg200dp = X.size(context, 200f, X.DP).toInt()
        (imagePreview.layoutParams as FrameLayout.LayoutParams).run {
            width = tg200dp
            height = tg200dp
        }
        imagePreview.setImageDrawable(context.getDrawable(R.drawable.img_gallery))
        val formatItems: Array<String> = if (X.aboveOn(X.P)) {
            arrayOf("png", "jpg", "webp", "heif")
        } else {
            arrayOf("png", "jpg", "webp")
        }
        toolsImageFormat.setText(formatItems[0])
        toolsImageFormat.dropDownBackground.setTint(ThemeUtil.getColor(context, R.attr.colorASurface))
        toolsImageFormat.setAdapter(ArrayAdapter(context, R.layout.pop_list_item, formatItems))
        image = null
        val onSeek = object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val v = when(p0?.id){
                    MyR.id.imageBlur -> imageBlurValue
                    MyR.id.imageCompress -> imageCompressValue
                    else -> null
                } as AppCompatTextView? ?: return
                v.dartFuture(String.format("%d/100", p1))
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        }
        imageBlur.setOnSeekBarChangeListener(onSeek)
        imageCompress.setOnSeekBarChangeListener(onSeek)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permission_REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isEmpty()) return
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val context = context ?: return
                    image ?: return
                    actionDone(context)
                } else {
                    notifyBriefly(R.string.toast_permission_storage_denied)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val context = context ?: return
        val activity = activity ?: return
        if (requestCode == REQUEST_GET_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val dataUri = data.data ?: return
            MemoryManager.clearSpace(activity)
            try {
                image = ImageUtil.getBitmap(context, dataUri)
            } catch (e: IOException) { e.printStackTrace() }

            val tg200dp = X.size(context, 200f, X.DP).toInt()
            (imagePreview.layoutParams as FrameLayout.LayoutParams).run {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            imagePreview.setImageBitmap(X.toMax(image!!, tg200dp))
            imageCard.cardElevation = X.size(context, 4f, X.DP)
            imageEditWidth.setText(image!!.width.toString())
            imageEditHeight.setText(image!!.height.toString())
            var nameCursor: Cursor? = null
            if (data.data != null)
                nameCursor = activity.contentResolver.query(data.data!!, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (nameCursor != null) {
                nameCursor.moveToFirst()
                imageName = nameCursor.getString(0)
                nameCursor.close()
            }
        }
    }

    private fun actionDone(context: Context){
        if (X.belowOff(X.Q)){
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (PermissionUtil.check(context, permissions).isNotEmpty()) {
                if (X.aboveOn(X.M)) {
                    requestPermissions(permissions, permission_REQUEST_EXTERNAL_STORAGE)
                } else {
                    notifyBriefly(R.string.toast_permission_storage_denied)
                }
                return
            }
        }

        val popLoading = CollisionDialog.loading(context, ProgressBar(context))
        popLoading.show()
        GlobalScope.launch {
            val isSuccessful = saveImage(context, processImage(context))
            if (isSuccessful) {
                notify(MyR.string.im_toast_finish)
            } else {
                notifyBriefly(R.string.text_error)
            }
        }.invokeOnCompletion {
            popLoading.dismiss()
        }
    }

    private fun processImage(context: Context): Bitmap{
        val tX: Int = if (!imageEditWidth.text.isNullOrEmpty())
            imageEditWidth.text!!.toString().toInt().let {
                if (it < 8000) it
                else {
                    X.toast(context, "limit width 8000", Toast.LENGTH_SHORT)
                    image!!.width
                }
            }
        else image!!.width
        val tH: Int = if (!imageEditHeight.text.isNullOrEmpty())
            imageEditHeight.text!!.toString().toInt().let {
                if (it < 8000) it
                else {
                    X.toast(context, "limit height 8000", Toast.LENGTH_SHORT)
                    image!!.height
                }
            }
        else image!!.height
        var image = X.toTarget(image!!, tX, tH)
        val blurDegree = imageBlur.progress / 4f
        if (blurDegree != 0f)
            X.toMin(image.collisionBitmap, 100).let {
                GaussianBlur(context).blurOnce(it, blurDegree)
            }.let { image = X.toTarget(it, image.width, image.height) }
//                image = X.toTarget(GaussianBlur(context).blurOnce(X.toMin(image.collisionBitmap, 100), blurDegree), image.width, image.height)
        return image
    }

    private fun saveImage(context: Context, image: Bitmap): Boolean{
        val formatExtension: String = toolsImageFormat.text.toString()
        var isHeif = false
        var name = imageName.substring(0, imageName.lastIndexOf("."))
        val formatMimeType: String
        var format: Bitmap.CompressFormat? = null
        when (formatExtension) {
            "jpg" -> {
                formatMimeType = "image/jpeg"
                format = Bitmap.CompressFormat.JPEG
            }
            "webp" -> {
                formatMimeType = "image/webp"
                format = Bitmap.CompressFormat.WEBP
            }
            "heif" -> {
                formatMimeType = "image/heif"
                isHeif = true
            }
            else -> {
                formatMimeType = "image/png"
                format = Bitmap.CompressFormat.PNG
            }
        }
        name += "-modified.$formatExtension"
        var imagePath = ""
        val uri = if (X.aboveOn(X.Q)) getImageUri4Q(context, name, formatMimeType) else {
            imagePath = F.createPath(F.externalRoot(Environment.DIRECTORY_PICTURES), name)
            File(imagePath).getProviderUri(context)
        }
        val fd = if (uri == null) null else try {
            context.contentResolver.openFileDescriptor(uri, "w")?.fileDescriptor
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
        val compressQuality = imageCompress.progress
        val isSucceful = fd?.let {
            if (isHeif) {
                if (X.aboveOn(X.P)) saveAsHeif(image, fd, compressQuality) else false
            } else saveAsNonHeif(image, fd, compressQuality, format!!, formatMimeType, imagePath)
        } ?: false
        if (!isSucceful && X.aboveOn(X.Q) && uri != null) {
            context.contentResolver.delete(uri, null, null)
        }
        return isSucceful
    }

    @RequiresApi(value = Build.VERSION_CODES.Q)
    private fun getImageUri4Q(context: Context, name: String, formatMimeType: String): Uri?{
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, formatMimeType)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        return context.contentResolver.insert(collection, values)
    }

    private fun saveAsNonHeif(image: Bitmap, fd: FileDescriptor, compressQuality: Int, format: Bitmap.CompressFormat, formatMimeType: String, imagePath: String): Boolean{
        try {
            FileOutputStream(fd).use {
                image.compress(format, compressQuality, it)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        }
        if (X.belowOff(X.Q)) MediaScannerConnection.scanFile(activity, arrayOf(imagePath), arrayOf(formatMimeType), null)
        return true
    }

    private fun saveAsHeif(bitmap: Bitmap, fd: FileDescriptor, compressQuality: Int): Boolean{
        try {
            HeifWriter.Builder(fd, bitmap.width, bitmap.height, HeifWriter.INPUT_MODE_BITMAP)
                    .setQuality(compressQuality).build().run {
                        start()
                        addBitmap(bitmap)
                        stop(0)
                        close()
                    }
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }
        return true
    }
}
