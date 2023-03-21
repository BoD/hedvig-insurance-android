package com.hedvig.android.sample.design.showcase.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2Buttons
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2Cards
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2Checkbox
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2Chips
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2Divider
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2ProgressBar
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2Slider
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2Tab
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2TextFields
import com.hedvig.android.sample.design.showcase.ui.m2.components.M2TopAppBars
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Buttons
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Cards
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Checkbox
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Chips
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3DatePicker
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Divider
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3NavigationBars
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3ProgressBar
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Slider
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Switch
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Tab
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3TextFields
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3TopAppBars

@Composable
fun MaterialComponents(windowSizeClass: WindowSizeClass) {
  if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
    BothThemes()
  } else {
    ThemeSelection()
  }
}

@Composable
private fun BothThemes() {
  Row(Modifier.fillMaxSize()) {
    Column(Modifier.weight(1f)) {
      Text("M2")
      M2()
    }
    Column(Modifier.weight(1f)) {
      Text("M3")
      M3()
    }
  }
}

@Composable
private fun ThemeSelection() {
  var isM3: Boolean? by remember { mutableStateOf(null) }
  when (isM3) {
    true -> {
      BackHandler { isM3 = null }
      M3()
    }

    false -> {
      BackHandler { isM3 = null }
      M2()
    }

    null -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Surface {
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { isM3 = false }) {
              Text("M2")
            }
            Button(onClick = { isM3 = true }) {
              Text("M3")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun M2() {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = rememberLazyListState(),
  ) {
    M2LightAndDarkItem { M2Buttons() }
    M2LightAndDarkItem { M2TextFields() }
    M2LightAndDarkItem { M2Chips() }
    M2LightAndDarkItem { M2Checkbox() }
    M2LightAndDarkItem { M2Slider() }
    M2LightAndDarkItem { M2ProgressBar() }
    M2LightAndDarkItem { M2Divider() }
    M2LightAndDarkItem { M2Cards() }
    M2LightAndDarkItem { M2TopAppBars() }
    M2LightAndDarkItem { M2Tab() }
  }
}

@Composable
private fun M3() {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = rememberLazyListState(),
  ) {
    LightAndDarkItem { M3Buttons() }
    LightAndDarkItem { M3TextFields() }
    LightAndDarkItem { M3Chips() }
    LightAndDarkItem { M3Checkbox() }
    LightAndDarkItem { M3Slider() }
    LightAndDarkItem { M3ProgressBar() }
    LightAndDarkItem { M3Divider() }
    LightAndDarkItem { M3Cards() }
    LightAndDarkItem { M3TopAppBars() }
    LightAndDarkItem { M3NavigationBars() }
    LightAndDarkItem { M3Tab() }
    LightAndDarkItem { M3Switch() } // We don't use this in m2
    LightAndDarkItem { M3DatePicker() } // We don't use this in m2
  }
}

@Suppress("FunctionName")
fun LazyListScope.M2LightAndDarkItem(content: @Composable () -> Unit) {
  item {
    Row(Modifier.fillMaxWidth()) {
      Box(Modifier.weight(1f)) {
        HedvigTheme(false) {
          Surface(Modifier.fillMaxWidth()) {
            content()
          }
        }
      }
      Box(Modifier.weight(1f)) {
        HedvigTheme(true) {
          Surface(Modifier.fillMaxWidth()) {
            content()
          }
        }
      }
    }
  }
}

@Suppress("FunctionName")
fun LazyListScope.LightAndDarkItem(content: @Composable () -> Unit) {
  item {
    Row(Modifier.fillMaxWidth()) {
      Box(Modifier.weight(1f)) {
        HedvigTheme(false) {
          Surface(Modifier.fillMaxWidth()) {
            content()
          }
        }
      }
      Box(Modifier.weight(1f)) {
        HedvigTheme(true) {
          Surface(Modifier.fillMaxWidth()) {
            content()
          }
        }
      }
    }
  }
}