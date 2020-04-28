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

package com.madness.collision.main

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.madness.collision.BuildConfig
import com.madness.collision.R
import com.madness.collision.diy.WindowInsets
import com.madness.collision.instant.Instant
import com.madness.collision.settings.SettingsFunc
import com.madness.collision.util.*
import kotlinx.android.synthetic.main.activity_immortal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

internal class ImmortalActivity : AppCompatActivity(), View.OnClickListener {
    private var logFile: File? = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.immortalRestart -> finish()

            R.id.immortalBagShare -> {
                if (logFile != null){
                    supportFragmentManager.let {
                        FilePop.by(this, logFile!!, "text/html", R.string.immortalShareTitle).show(it, FilePop.TAG)
                    }
                } else {
                    CollisionDialog.alert(this, R.string.textWaitASecond).show()
                }
            }

            R.id.immortalBagSend -> {
                if (logFile != null){
                    val title = getString(R.string.immortalSendTitle)
                    startActivity(Intent().apply {
                        action = Intent.ACTION_SENDTO
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        data = Uri.parse("mailto:" + P.CONTACT_EMAIL)
//                        putExtra(Intent.EXTRA_EMAIL, arrayOf(P.CONTACT_EMAIL))
                        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile))
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.immortalEmailSubject))
//                        type = "text/plain"
                        val locales = getLocaleTags()
                        val body = "\n\nApp: ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})\nLocales: $locales\n\n"
                        putExtra(Intent.EXTRA_TEXT, body)
                        putExtra(Intent.EXTRA_TITLE, title) // android q title
                    }.let { Intent.createChooser(it, title) }) // deprecated in android 10
                } else {
                    CollisionDialog.alert(this, R.string.textWaitASecond).show()
                }
            }

            R.id.immortalContactEmail -> {
                try {
                    startActivity(Intent().apply {
                        action = Intent.ACTION_SENDTO
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        data = Uri.parse("mailto:" + P.CONTACT_EMAIL)
                    })
                } catch (e: Exception) {
                    CollisionDialog.infoCopyable(this, P.CONTACT_EMAIL).show()
                }
            }

            R.id.immortalContactQQ -> {
                try {
                    startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        data = Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=${P.CONTACT_QQ}&version=1")
                    })
                    X.toast(this, R.string.Advice_QQ_Toast_Text, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    CollisionDialog.infoCopyable(this, P.CONTACT_QQ).show()
                }
            }
        }
    }

    private fun getLocaleTags(): String {
        return if (X.aboveOn(X.N)) LocaleList.getDefault().toLanguageTags() else Locale.getDefault().toLanguageTag()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("Immortal", "onCreate()")
        // true: from settings, false: from crash
        val isMortal = intent.getStringExtra(P.IMMORTAL_EXTRA_LAUNCH_MODE) == P.IMMORTAL_EXTRA_LAUNCH_MODE_MORTAL
        ThemeUtil.updateTheme(this, getSharedPreferences(P.PREF_SETTINGS, Context.MODE_PRIVATE))
        window?.let { SystemUtil.applyEdge2Edge(it) }
        SettingsFunc.updateLanguage(this)
        setContentView(R.layout.activity_immortal)
        applyInsets()
        window?.let {
            val darkBar = mainApplication.isPaleTheme
            SystemUtil.applyStatusBarColor(this, it, darkBar, true)
            SystemUtil.applyNavBarColor(this, it, darkBar, true)
        }
        if (isMortal) immortalMessage.setText(R.string.textWaitASecond)
        GlobalScope.launch {
            try {
                logFile = log()
                launch(Dispatchers.Main) {
                    if (isMortal) immortalMessage.setText(R.string.immortalMessageMortal)
                }
            }catch (e: Exception){
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    immortalMessage.setText(R.string.text_error)
                }
            }
        }
        (immortalLogo.drawable as AnimatedVectorDrawable).start()
        immortalLogo.setOnLongClickListener {
            if (X.belowOff(X.N_MR1)) return@setOnLongClickListener true
            val manager = getSystemService(ShortcutManager::class.java) ?: return@setOnLongClickListener true
            Instant(this, manager).let {
                val isExisting = it.dynamicShortcuts.find { s ->
                    s.id == P.SC_ID_IMMORTAL
                } != null
                if (isExisting) it.removeDynamicShortcuts(P.SC_ID_IMMORTAL)
                else it.addDynamicShortcuts(P.SC_ID_IMMORTAL)
            }
            notifyBriefly(R.string.text_done)
            true
        }
    }

    private fun applyInsets(){
        immortalRoot.setOnApplyWindowInsetsListener { _, insets ->
            consumeInsets(WindowInsets(insets))
            insets
        }
    }

    private fun consumeInsets(insets: WindowInsets) {
        mainApplication.insetTop = insets.top
        mainApplication.insetBottom = insets.bottom
        mainApplication.insetLeft = insets.left
        mainApplication.insetRight = insets.right
        immortalRoot.alterPadding(start = insets.left, end = insets.right)
    }

    private fun log(): File {
        // - device info
        val manufacture = Build.MANUFACTURER
        val model = Build.MODEL
        val product = Build.PRODUCT
        val device = Build.DEVICE
        // - device model info

        // - app version info
//        val packageInfo = MiscApp.getPackageInfo(this, packageName = packageName) ?: return File("")
//        val ver = PackageInfoCompat.getLongVersionCode(packageInfo).toString()
        val ver = BuildConfig.VERSION_CODE
        val verName = BuildConfig.VERSION_NAME
        // - app version info

        val locales = getLocaleTags()

        val apiLevel = Build.VERSION.SDK_INT

        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", SystemUtil.getLocaleApp()).format(Calendar.getInstance().time)
        val logFile = F.createFile(
                F.cachePublicPath(this),
                P.DIR_NAME_LOG,
                "Boundo $verName($ver) @$time $manufacture $model $apiLevel.html"
        )

        // - clear extra log files
        logFile.parentFile?.deleteRecursively()

        if (!F.prepare4(logFile)) return File("")
        val writer = FileWriter(logFile)
        writer.write(wrapInHtml("Manufacture: $manufacture\nModel: $model\nProduct: $product\nDevice: $device\nAPI: $apiLevel\nApp: $verName($ver)\nLocales: $locales\n"))

        // - write logcat
        val process = Runtime.getRuntime().exec("logcat -d -v time")
        val reader = InputStreamReader(process.inputStream)
        reader.useLines { it.forEach {  line -> writer.write(wrapInHtml(line)) } }
        // - write logcat

        writer.close()

        Log.i("Immortal", "Log: ${logFile.path}")

        return logFile
    }

    private fun wrapInHtml(line: String): String{
        val re = SpannableStringBuilder(line)
        when{
            re.matches("[\\d- :.]*E/.*".toRegex()) -> re.highlightAll(Color.RED)
            re.matches("[\\d- :.]*W/.*".toRegex()) -> re.highlightAll(Color.MAGENTA)
        }
        if (re.matches("[\\d- :.]*./dness.collisio.*".toRegex())) re.highlight("dness.collisio", Color.BLUE)
        if (BuildConfig.DEBUG && re.matches("[\\d- :.]*./ollision.morta.*".toRegex())) re.highlight("ollision.morta", Color.BLUE)
        re.highlight(BuildConfig.BUILD_PACKAGE, Color.BLUE)
        re.appendln()
        return HtmlCompat.toHtml(SpannedString(re), HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
    }

    private fun SpannableStringBuilder.highlightAll(color: Int){
        setSpan(ForegroundColorSpan(color), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun SpannableStringBuilder.highlight(content: String, color: Int){
        var indexEnd = 0
        while (true) {
            val indexStart = indexOf(content, indexEnd)
            if (indexStart == -1) break
            indexEnd = indexStart + content.length
            setSpan(ForegroundColorSpan(color), indexStart, indexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
