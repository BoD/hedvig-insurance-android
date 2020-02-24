package com.hedvig.app.feature.keygear.data

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import java.security.MessageDigest
import kotlin.math.pow
import kotlin.math.sqrt

class DeviceInformationService(
    private val context: Context
) {
    @SuppressLint("HardwareIds")
    fun getDeviceFingerprint(): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.reset()

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        messageDigest.update(androidId.toByteArray())

        val result = messageDigest.digest()
        return result.toString()
    }

    fun getDeviceType(): DeviceType {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        when (uiModeManager.currentModeType) {
            Configuration.UI_MODE_TYPE_TELEVISION -> return DeviceType.TV
            Configuration.UI_MODE_TYPE_WATCH -> return DeviceType.WATCH
        }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        val widthInches = displayMetrics.widthPixels / displayMetrics.xdpi
        val heightInches = displayMetrics.heightPixels / displayMetrics.xdpi

        val diagonalInches = sqrt(widthInches.pow(2) + heightInches.pow(2))

        // This is unfortunately the only trick that is available
        if (diagonalInches >= 7.0f) {
            return DeviceType.TABLET
        }

        return DeviceType.PHONE
    }
}

enum class DeviceType {
    PHONE,
    TABLET,
    WATCH,
    TV;

    fun into() = when (this) {
        PHONE -> KeyGearItemCategory.PHONE
        TABLET -> TODO()
        WATCH -> KeyGearItemCategory.SMART_WATCH
        TV -> KeyGearItemCategory.TV
    }
}
