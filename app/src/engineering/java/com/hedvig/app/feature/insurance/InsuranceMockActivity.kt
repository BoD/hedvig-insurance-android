package com.hedvig.app.feature.insurance

import com.hedvig.app.MockActivity
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_TRAVEL
import com.hedvig.app.MockInsuranceViewModel.Companion.SWEDISH_HOUSE
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.MockLoggedInViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.insuranceModule
import com.hedvig.app.loggedInModule
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ACTIVE_AND_TERMINATED
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_NO_RENEWAL
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_STUDENT
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class InsuranceMockActivity : MockActivity() {
    override val original = listOf(
        loggedInModule,
        insuranceModule
    )
    override val mocks = listOf(
        module {
            viewModel<LoggedInViewModel> { MockLoggedInViewModel() }
            viewModel<InsuranceViewModel> { MockInsuranceViewModel() }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Tab Screen")
        clickableItem("Active on and Terminated on") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_ACTIVE_AND_TERMINATED
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Student") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_STUDENT
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Renewal /w SE apartment") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Renewal /w SE house") {
            MockInsuranceViewModel.apply {
                insuranceMockData = SWEDISH_HOUSE
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("No Renewal /w SE apartment") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_NO_RENEWAL
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian travel and home contract") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian travel") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_TRAVEL
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian home") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_HOME_CONTENTS
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian home Error") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_HOME_CONTENTS
                shouldError = true
            }
            startActivity(
                LoggedInActivity.newInstance(
                    this@InsuranceMockActivity,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        header("Detail Screen")
        clickableItem("No particular data") {
            startActivity(ContractDetailActivity.newInstance(this@InsuranceMockActivity))
        }
    }
}
