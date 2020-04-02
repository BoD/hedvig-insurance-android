package com.hedvig.app.viewgallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.debug.activity_view_gallery.*

class ViewGalleryActivity : AppCompatActivity(R.layout.activity_view_gallery) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.useEdgeToEdge()
        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }
        scrollView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }
        scrollViewContent.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        openBottomSheet.setHapticClickListener {
            ViewGalleryBottomSheet
                .newInstance()
                .show(supportFragmentManager)
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ViewGalleryActivity::class.java)
    }
}
