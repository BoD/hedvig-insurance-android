package com.hedvig.app.feature.onbarding.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.updatePaddingRelative
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityChoosePlanBinding
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.onbarding.ChoosePlanViewModel
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ChoosePlanActivity : BaseActivity(R.layout.activity_choose_plan) {
    private val binding by viewBinding(ActivityChoosePlanBinding::bind)
    private val marketProvider: MarketProvider by inject()
    private val viewModel: ChoosePlanViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            recycler.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

            recycler.itemAnimator = ViewHolderReusingDefaultItemAnimator()
            recycler.adapter = OnboardingAdapter(viewModel, marketProvider)
            viewModel.data.observe(this@ChoosePlanActivity) { response ->
                if (response.isFailure) {
                    TODO("Implement error view")
                    return@observe
                }
                val bundles = response.getOrNull()?.embarkStories ?: return@observe
                getMobileTypes(bundles)

                (recycler.adapter as OnboardingAdapter).submitList(listOf())
            }
            viewModel.load()
            /*viewModel.selectedQuoteType.observe(this@ChoosePlanActivity) { quote ->
                (recycler.adapter as OnboardingAdapter).submitList(
                    listOf(
                        OnboardingModel.Quote.Bundle(quote is OnboardingModel.Quote.Bundle),
                        OnboardingModel.Quote.Content(quote is OnboardingModel.Quote.Content),
                        OnboardingModel.Quote.Travel(quote is OnboardingModel.Quote.Travel),
                        OnboardingModel.Info,
                        OnboardingModel.Button
                    )
                )
            }*/
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.choose_plan_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.moreOptions -> {
            startActivity(MoreOptionsActivity.newInstance(this))
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val COMBO = "COMBO"
        private const val CONTENTS = "CONTENTS"
        private const val TRAVEL = "TRAVEL"

        fun newInstance(context: Context) = Intent(context, ChoosePlanActivity::class.java)

        private fun getMobileTypes(bundles: List<ChoosePlanQuery.EmbarkStory>) =
            bundles.filter { it.type == EmbarkStoryType.APP_ONBOARDING }.map { embarkStory->
                if (embarkStory.name.contains(COMBO)) {

                }
            }
    }
}
