package com.hedvig.app.feature.embark.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.updatePaddingRelative
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMoreOptionsBinding
import com.hedvig.app.feature.embark.MoreOptionsViewModel
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.viewmodel.ext.android.viewModel

class MoreOptionsActivity : BaseActivity(R.layout.activity_more_options) {
    private val binding by viewBinding(ActivityMoreOptionsBinding::bind)
    private val viewModel: MoreOptionsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            restart.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }
            setSupportActionBar(toolbar)

            recycler.adapter = MoreOptionsAdapter(viewModel)

            viewModel.data.observe(this@MoreOptionsActivity) { result ->
                if (result.isSuccess) {
                    (recycler.adapter as MoreOptionsAdapter).submitList(
                        listOf(
                            MoreOptionsModel.Header,
                            result.getOrNull()?.member?.id?.let { MoreOptionsModel.UserId.Success(it) },
                            MoreOptionsModel.Version,
                            MoreOptionsModel.Settings,
                            MoreOptionsModel.Copyright
                        )
                    )
                } else {
                    (recycler.adapter as MoreOptionsAdapter).submitList(
                        listOf(
                            MoreOptionsModel.Header,
                            MoreOptionsModel.UserId.Error,
                            MoreOptionsModel.Version,
                            MoreOptionsModel.Settings,
                            MoreOptionsModel.Copyright
                        )
                    )
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, MoreOptionsActivity::class.java)
    }
}
