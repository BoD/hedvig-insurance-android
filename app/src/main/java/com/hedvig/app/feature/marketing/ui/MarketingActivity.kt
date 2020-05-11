package com.hedvig.app.feature.marketing.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketPickerActivity
import com.hedvig.app.feature.norway.NorwegianAuthenticationActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.util.BlurHashDecoder
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_marketing.*
import org.koin.android.viewmodel.ext.android.viewModel

class MarketingActivity : BaseActivity(R.layout.activity_marketing) {
    private val model: MarketingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root.setEdgeToEdgeSystemUiFlags(true)

        legal.doOnApplyWindowInsets { view, insets, initialState ->
            view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
        }

        settings.doOnApplyWindowInsets { view, insets, initialState ->
            view.updateMargin(top = initialState.margins.top + insets.systemWindowInsetTop)
        }

        val market = getMarket()
        if (market == null) {
            startActivity(MarketPickerActivity.newInstance(this))
            return
        }

        settings.setHapticClickListener {
            startActivity(SettingsActivity.newInstance(this))
        }

        signUp.setHapticClickListener {
            when (market) {
                Market.SE -> startActivity(
                    ChatActivity.newInstance(this)
                        .apply { putExtra(ChatActivity.EXTRA_SHOW_RESTART, true) })
                Market.NO -> {
                    startActivity(WebOnboardingActivity.newInstance(this))
                }
            }
        }

        logIn.setHapticClickListener {
            when (market) {
                Market.SE -> {
                    AuthenticateDialog().show(supportFragmentManager, AuthenticateDialog.TAG)
                }
                Market.NO -> {
                    startActivity(NorwegianAuthenticationActivity.newInstance(this))
                }
            }
        }

        model
            .marketingBackground
            .observe(this) { image ->
                image?.let { i ->
                    val placeholder = BlurHashDecoder.decode(i.blurhash, 32, 32)

                    Glide
                        .with(this)
                        .load(i.image?.url)
                        .transition(withCrossFade())
                        .placeholder(BitmapDrawable(resources, placeholder))
                        .into(backgroundImage)
                }
            }
    }

    companion object {
        fun newInstance(context: Context, withoutHistory: Boolean = false) =
            Intent(context, MarketingActivity::class.java).apply {
                if (withoutHistory) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }
    }
}
