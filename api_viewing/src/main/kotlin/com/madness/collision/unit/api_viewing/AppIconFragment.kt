package com.madness.collision.unit.api_viewing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.madness.collision.R
import com.madness.collision.main.MainViewModel
import com.madness.collision.misc.MiscApp
import com.madness.collision.unit.api_viewing.data.AppIcon
import com.madness.collision.util.*
import kotlinx.android.synthetic.main.dialog_api_sub_ai.*
import java.io.File
import com.madness.collision.unit.api_viewing.R as MyR

internal class AppIconFragment : DialogFragment() {
    companion object {
        const val ARG_APP_NAME = "Name"
        const val ARG_PACKAGE_NAME = "PackageName"
        const val ARG_APK_Path = "ApkPath"
        const val ARG_IS_ARCHIVE = "IsArchive"
        const val TAG = "AppIconFragment"

        @JvmStatic
        fun newInstance(appName: String, packageName: String, path: String, isArchive: Boolean) = AppIconFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_APP_NAME, appName)
                putString(ARG_PACKAGE_NAME, packageName)
                putString(ARG_APK_Path, path)
                putBoolean(ARG_IS_ARCHIVE, isArchive)
            }
        }
    }

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(MyR.layout.dialog_api_sub_ai, container, true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = context ?: return

        dialog?.window?.let {
            SystemUtil.applyEdge2Edge(it)
            SystemUtil.applyDefaultSystemUiVisibility(context, it, mainViewModel.insetBottom.value ?: 0)
        }

        mainViewModel.insetTop.observe(viewLifecycleOwner) {
            apiInfoAiGuideTop.setGuidelineBegin(it)
        }
        mainViewModel.insetBottom.observe(viewLifecycleOwner) {
            apiInfoAiGuideBottom.setGuidelineEnd(it)
//            (apiInfoAiSpace.layoutParams as ConstraintLayout.LayoutParams).height = it
//            apiInfoAiSpace.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, it)
        }
        mainViewModel.insetLeft.observe(viewLifecycleOwner){
            avInfoAiRoot.alterPadding(start = it)
        }
        mainViewModel.insetRight.observe(viewLifecycleOwner){
            avInfoAiRoot.alterPadding(end = it)
        }

        val appName = arguments?.getString(ARG_APP_NAME) ?: ""
        val packageName = arguments?.getString(ARG_PACKAGE_NAME) ?: ""
        val apkPath = arguments?.getString(ARG_APK_Path) ?: ""
        val isArchive = arguments?.getBoolean(ARG_IS_ARCHIVE) ?: false
        val applicationInfo = if (isArchive) MiscApp.getApplicationInfo(context, apkPath = apkPath)
        else MiscApp.getApplicationInfo(context, packageName = packageName)
        applicationInfo ?: return
        var dIcon: Drawable? = null
        var dIconR: Drawable? = null
        val res = context.packageManager.getResourcesForApplication(applicationInfo)
        try {
            val resID = ManifestUtil.getManifestAttr(apkPath, arrayOf("application", "icon"))
            if (resID.isNotEmpty()) dIcon = res.getDrawable(resID.toInt(), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val resID = ManifestUtil.getManifestAttr(apkPath, arrayOf("application", "roundIcon"))
            if (resID.isNotEmpty()) dIconR = res.getDrawable(resID.toInt(), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (dIcon == null) {
            dIcon = applicationInfo.loadIcon(context.packageManager)
            apiInfoAiIconDes.setText(MyR.string.apiInfoAiIconDesSys)
        }
        if (dIcon != null){
            val icon = AppIcon(context, dIcon)
            if (icon.isAdaptive && X.aboveOn(X.O)) {
                val logoDrawable = icon.drawable as AdaptiveIconDrawable
                apiInfoAiIcon.setImageBitmap(icon.bitmap)
                apiInfoAiIcon.setOnClickListener { actionIcon(context, icon.bitmap, "$appName-Full") }
                apiInfoAiIconFore.setImageDrawable(logoDrawable.foreground)
                apiInfoAiIconFore.setOnClickListener { actionIcon(context, logoDrawable.foreground, "$appName-Fore") }
                apiInfoAiIconBack.setImageDrawable(logoDrawable.background)
                apiInfoAiIconBack.setOnClickListener { actionIcon(context, logoDrawable.background, "$appName-Back") }

                val dp80 = X.size(context, 72f, X.DP).toInt()
                val drawableRound = BitmapDrawable(context.resources, icon.round)
                drawableRound.setBounds(0, 0, dp80, dp80)
                apiInfoAiIconRound.setCompoundDrawablesRelative(null, drawableRound, null, null)
                apiInfoAiIconRound.setOnClickListener { actionIcon(context, icon.round, "$appName-Round") }

                val drawableRounded = BitmapDrawable(context.resources, icon.rounded)
                drawableRounded.setBounds(0, 0, dp80, dp80)
                apiInfoAiIconRounded.setCompoundDrawablesRelative(null, drawableRounded, null, null)
                apiInfoAiIconRounded.setOnClickListener { actionIcon(context, icon.rounded, "$appName-Rounded") }

                val drawableSquircle = BitmapDrawable(context.resources, icon.squircle)
                drawableSquircle.setBounds(0, 0, dp80, dp80)
                apiInfoAiIconSquircle.setCompoundDrawablesRelative(null, drawableSquircle, null, null)
                apiInfoAiIconSquircle.setOnClickListener { actionIcon(context, icon.squircle, "$appName-Squircle") }
            } else {
                apiInfoAiIcon.setImageBitmap(icon.bitmap)
                apiInfoAiIcon.setOnClickListener { actionIcon(context, icon.bitmap, "$appName-Full") }
                X.makeGone(apiInfoAiIconGroupAi)
            }
        }else{
            X.makeGone(apiInfoAiIconGroup, apiInfoAiIconGroupAi)
        }
        if (dIconR != null){
            val icon = AppIcon(context, dIconR)
            if (icon.isAdaptive && X.aboveOn(X.O)) {
                val logoDrawable = icon.drawable as AdaptiveIconDrawable
                apiInfoAiIconR.setImageBitmap(icon.bitmap)
                apiInfoAiIconR.setOnClickListener { actionIcon(context, icon.bitmap, "$appName-R-Full") }
                apiInfoAiIconRFore.setImageDrawable(logoDrawable.foreground)
                apiInfoAiIconRFore.setOnClickListener { actionIcon(context, logoDrawable.foreground, "$appName-R-Fore") }
                apiInfoAiIconRBack.setImageDrawable(logoDrawable.background)
                apiInfoAiIconRBack.setOnClickListener { actionIcon(context, logoDrawable.background, "$appName-R-Back") }

                val dp80 = X.size(context, 72f, X.DP).toInt()
                val drawableRound = BitmapDrawable(context.resources, icon.round)
                drawableRound.setBounds(0, 0, dp80, dp80)
                apiInfoAiIconRRound.setCompoundDrawablesRelative(null, drawableRound, null, null)
                apiInfoAiIconRRound.setOnClickListener { actionIcon(context, icon.round, "$appName-R-Round") }

                val drawableRounded = BitmapDrawable(context.resources, icon.rounded)
                drawableRounded.setBounds(0, 0, dp80, dp80)
                apiInfoAiIconRRounded.setCompoundDrawablesRelative(null, drawableRounded, null, null)
                apiInfoAiIconRRounded.setOnClickListener { actionIcon(context, icon.rounded, "$appName-R-Rounded") }

                val drawableSquircle = BitmapDrawable(context.resources, icon.squircle)
                drawableSquircle.setBounds(0, 0, dp80, dp80)
                apiInfoAiIconRSquircle.setCompoundDrawablesRelative(null, drawableSquircle, null, null)
                apiInfoAiIconRSquircle.setOnClickListener { actionIcon(context, icon.squircle, "$appName-R-Squircle") }
            } else {
                apiInfoAiIconR.setImageBitmap(icon.bitmap)
                apiInfoAiIconR.setOnClickListener { actionIcon(context, icon.bitmap, "$appName-R-Full") }
                X.makeGone(apiInfoAiIconRGroupAi)
            }
        }else {
            X.makeGone(apiInfoAiIconRGroup, apiInfoAiIconRGroupAi)
        }

        apiInfoAiOverallDes.setOnClickListener{
            CollisionDialog.alert(context, MyR.string.apiInfoAiOverallDes).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (dialog == null) return
        dialog!!.window?.run {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setWindowAnimations(R.style.AppTheme_PopUp)
        }
    }

    private fun actionIcon(context: Context, imgIcon: Drawable, exportName: String){
        actionIcon(context, X.drawableToBitmap(imgIcon), exportName)
    }

    private fun actionIcon(context: Context, imgIcon: Bitmap, exportName: String){
        val path = F.createPath(F.cachePublicPath(context), "App", "Logo", "${exportName}.png")
        val image = File(path)
        imgIcon.let { if (F.prepare4(image)) X.savePNG(it, path) }
        val uri: Uri = image.getProviderUri(context)
        childFragmentManager.let {
            FilePop.by(context, uri, "image/*", R.string.textShareImage, uri, exportName).show(it, FilePop.TAG)
        }
    }

}
