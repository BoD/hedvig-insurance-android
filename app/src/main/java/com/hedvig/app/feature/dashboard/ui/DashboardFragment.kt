package com.hedvig.app.feature.dashboard.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.view.updatePadding
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val tracker: DashboardTracker by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scrollInitialBottomPadding = root.paddingBottom
        loggedInViewModel.bottomTabInset.observe(this) { bti ->
            bti?.let { bottomTabInset ->
                root.updatePadding(bottom = scrollInitialBottomPadding + bottomTabInset)
            }
        }

        root.setupToolbarScrollListener(loggedInViewModel)
        root.adapter = DashboardAdapter(parentFragmentManager, tracker)

        dashboardViewModel.data.observe(this) { data ->
            data?.let { bind(it) }
        }
    }

    private fun bind(data: Pair<DashboardQuery.Data?, PayinStatusQuery.Data?>) {
        loadingSpinner.remove()
        val (dashboardData, payinStatusData) = data

        /*
        val infoBoxes = mutableListOf<DashboardModel.InfoBox>()

        dashboardData?.importantMessages?.firstOrNull()?.let { importantMessage ->
            infoBoxes.add(
                DashboardModel.InfoBox.ImportantInformation(
                    importantMessage.title ?: "",
                    importantMessage.message ?: "",
                    importantMessage.button ?: "",
                    importantMessage.link ?: ""
                )
            )
        }

        val renewals = dashboardData?.contracts.orEmpty().mapNotNull { it.upcomingRenewal }

        renewals.forEach {
            infoBoxes.add(
                DashboardModel.InfoBox.Renewal(
                    it.renewalDate,
                    it.draftCertificateUrl
                )
            )
        }

        if (payinStatusData?.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
            infoBoxes.add(DashboardModel.InfoBox.ConnectPayin)
        }
         */

        val contracts = dashboardData?.contracts.orEmpty().map { DashboardModel.Contract(it) }

        val upsells = mutableListOf<DashboardModel.Upsell>()
        dashboardData?.let { dd ->
            if (isNorway(dd.contracts)) {
                if (doesNotHaveHomeContents(dd.contracts)) {
                    upsells.add(UPSELL_HOME_CONTENTS)
                } else if (doesNotHaveTravelInsurance(dd.contracts)) {
                    upsells.add(UPSELL_TRAVEL)
                }
            }
        }

        (root.adapter as? DashboardAdapter)?.items =
            listOf(DashboardModel.Header) + contracts + upsells
    }

    override fun onResume() {
        super.onResume()
        root.scrollToPosition(0)
    }

    companion object {
        private val UPSELL_HOME_CONTENTS =
            DashboardModel.Upsell(
                R.string.UPSELL_NOTIFICATION_CONTENT_TITLE,
                R.string.UPSELL_NOTIFICATION_CONTENT_DESCRIPTION,
                R.string.UPSELL_NOTIFICATION_CONTENT_CTA
            )

        private val UPSELL_TRAVEL =
            DashboardModel.Upsell(
                R.string.UPSELL_NOTIFICATION_TRAVEL_TITLE,
                R.string.UPSELL_NOTIFICATION_TRAVEL_DESCRIPTION,
                R.string.UPSELL_NOTIFICATION_TRAVEL_CTA
            )

        fun isNorway(contracts: List<DashboardQuery.Contract>) =
            contracts.any { it.currentAgreement.asNorwegianTravelAgreement != null || it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveHomeContents(contracts: List<DashboardQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveTravelInsurance(contracts: List<DashboardQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianTravelAgreement != null }
    }
}
