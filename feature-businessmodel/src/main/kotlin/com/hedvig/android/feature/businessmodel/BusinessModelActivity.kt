package com.hedvig.android.feature.businessmodel

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.businessmodel.ui.BusinessModelScreen
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class BusinessModelActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val imageLoader: ImageLoader = get()
    getViewModel<BusinessModelViewModel>()
    setContent {
      HedvigTheme {
        BusinessModelScreen(
          navigateBack = { onBackPressed() },
          imageLoader = imageLoader,
          windowSizeClass = calculateWindowSizeClass(this),
        )
      }
    }
  }
}
