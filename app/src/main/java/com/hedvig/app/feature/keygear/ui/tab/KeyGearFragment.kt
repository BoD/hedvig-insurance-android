package com.hedvig.app.feature.keygear.ui.tab

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_QUINTUPLE
import com.hedvig.app.BASE_MARGIN_TRIPLE
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemActivity
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailActivity
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.ui.animator.SlideInItemAnimator
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.transitionPair
import kotlinx.android.synthetic.main.fragment_key_gear.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class KeyGearFragment : BaseTabFragment() {
    override val layout = R.layout.fragment_key_gear

    private val viewModel: KeyGearViewModel by sharedViewModel()
    private val tracker: KeyGearTracker by inject()

    private var hasSentAutoAddedItems = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items.adapter =
            KeyGearItemsAdapter(
                tracker,
                { v ->
                    startActivity(
                        CreateKeyGearItemActivity.newInstance(requireContext()),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            transitionPair(v)
                        ).toBundle()
                    )
                }, { root, item ->
                startActivity(
                    KeyGearItemDetailActivity.newInstance(
                        requireContext(),
                        item.fragments.keyGearItemFragment
                    ),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        Pair(root, ITEM_BACKGROUND_TRANSITION_NAME)
                    ).toBundle()
                )
            })
        items.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN))
        items.itemAnimator = SlideInItemAnimator()

        viewModel.data.observe(this) { d ->
            d?.let { data ->
                bind(data)
                if (!hasSentAutoAddedItems) {
                    hasSentAutoAddedItems = true
                    viewModel.sendAutoAddedItems()
                }
            }
        }
    }

    fun bind(data: KeyGearItemsQuery.Data) {
        loadingSpinner.remove()
        (items.adapter as? KeyGearItemsAdapter)?.items = data.keyGearItems
        items.show()

        if (data.keyGearItems.isEmpty() || !data.keyGearItems.any { it.fragments.keyGearItemFragment.physicalReferenceHash == null }) {
            illustration.show()
            title.show()
            description.show()
            items.updateMargin(top = BASE_MARGIN_QUINTUPLE)
        } else {
            illustration.remove()
            title.remove()
            description.remove()
            items.updateMargin(top = BASE_MARGIN_TRIPLE)
        }
    }

    companion object {
        const val ITEM_BACKGROUND_TRANSITION_NAME = "itemBackground"
    }
}