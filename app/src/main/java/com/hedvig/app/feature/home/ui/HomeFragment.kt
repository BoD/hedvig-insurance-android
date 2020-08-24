package com.hedvig.app.feature.home.ui

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeFragmentBinding
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.home_fragment) {
    private val model: HomeViewModel by viewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val binding by viewBinding(HomeFragmentBinding::bind)

    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()

    private var recyclerInitialPaddingBottom = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.recycler.apply {
            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            recyclerInitialPaddingBottom = paddingBottom
            loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
                updatePadding(bottom = recyclerInitialPaddingBottom + bottomTabInset)
            }
            adapter = HomeAdapter(parentFragmentManager, model::load, requestBuilder)
            (layoutManager as? GridLayoutManager)?.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        (binding.recycler.adapter as? HomeAdapter)?.items?.getOrNull(position)
                            ?.let { item ->
                                return when (item) {
                                    is HomeModel.CommonClaim -> 1
                                    else -> 2
                                }
                            }
                        return 2
                    }
                }
            addItemDecoration(HomeItemDecoration())
        }

        model.data.observe(viewLifecycleOwner) { (homeData, payinStatusData) ->
            if (homeData == null) {
                return@observe
            }
            if (homeData.isFailure) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.Error
                )
                return@observe
            }

            val successData = homeData.getOrNull() ?: return@observe
            val firstName = successData.member.firstName
            if (firstName == null) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.Error
                )
                return@observe
            }
            if (isPending(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.Pending(
                        firstName
                    ),

                    HomeModel.BodyText.Pending
                )
            }
            if (isActiveInFuture(successData.contracts)) {
                val firstInceptionDate = successData
                    .contracts
                    .mapNotNull {
                        it.status.asActiveInFutureStatus?.futureInception
                            ?: it.status.asActiveInFutureAndTerminatedInFutureStatus?.futureInception
                    }
                    .min()

                if (firstInceptionDate == null) {
                    (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                        HomeModel.Error
                    )
                    return@observe
                }

                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.ActiveInFuture(
                        firstName,
                        firstInceptionDate
                    ),
                    HomeModel.BodyText.ActiveInFuture
                )
            }

            if (isTerminated(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.Terminated(firstName),
                    HomeModel.StartClaimOutlined
                )
            }

            if (isActive(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOfNotNull(
                    HomeModel.BigText.Active(firstName),
                    HomeModel.StartClaimContained,
                    *psaItems(successData.importantMessages).toTypedArray(),
                    if (payinStatusData?.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
                        HomeModel.InfoCard.ConnectPayin
                    } else {
                        null
                    },
                    HomeModel.CommonClaimTitle,
                    *commonClaimsItems(
                        successData.commonClaims,
                        successData.isEligibleToCreateClaim
                    ).toTypedArray()
                )
            }
        }
    }

    private fun psaItems(
        importantMessages: List<HomeQuery.ImportantMessage?>
    ) = importantMessages
        .filterNotNull()
        .map { HomeModel.InfoCard.PSA(it) }

    private fun commonClaimsItems(
        commonClaims: List<HomeQuery.CommonClaim>,
        isEligibleToCreateClaim: Boolean
    ) =
        commonClaims.map { cc ->
            cc.layout.asEmergency?.let {
                EmergencyData.from(cc, isEligibleToCreateClaim)?.let { ed ->
                    return@map HomeModel.CommonClaim.Emergency(ed)
                }
            }
            cc.layout.asTitleAndBulletPoints?.let {
                CommonClaimsData.from(cc, isEligibleToCreateClaim)
                    ?.let { ccd ->
                        return@map HomeModel.CommonClaim.TitleAndBulletPoints(ccd)
                    }
            }
            null
        }

    companion object {
        private fun isPending(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asPendingStatus != null }

        private fun isActiveInFuture(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asActiveInFutureStatus != null || it.status.asActiveInFutureAndTerminatedInFutureStatus != null }

        private fun isActive(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asActiveStatus != null }

        private fun isTerminated(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asTerminatedStatus != null }
    }
}
