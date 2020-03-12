package com.hedvig.app

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.app.feature.dashboard.ui.Contract
import com.hedvig.app.feature.dashboard.ui.DashboardData
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import org.threeten.bp.LocalDate

class MockDashboardViewModel(context: Context) : DashboardViewModel() {
    override val data = MutableLiveData<DashboardData>()
    override val directDebitStatus = MutableLiveData<DirectDebitQuery.Data>()

    init {
        val activePersona = context
            .getSharedPreferences(DevelopmentActivity.DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)
        data.postValue(when (activePersona) {
            2 -> NORWEGIAN_HOME_CONTENTS
            3 -> NORWEGIAN_TRAVEL
            4 -> NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
            else -> NORWEGIAN_HOME_CONTENTS
        })

        directDebitStatus.postValue(DirectDebitQuery.Data(DirectDebitStatus.NEEDS_SETUP))
    }

    companion object {
        private val NORWEGIAN_HOME_CONTENTS_CONTRACT = Contract(
            "120e9ac9-84b1-4e5d-add1-70a9bad340be",
            ContractStatus.ACTIVE,
            LocalDate.of(2020, 1, 1),
            null,
            DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    status = AgreementStatus.ACTIVE,
                    activeFrom = LocalDate.of(2020, 1, 1),
                    activeTo = null
                ),
                asNorwegianHomeContentAgreement = DashboardQuery.AsNorwegianHomeContentAgreement(
                    address = DashboardQuery.Address2(
                        fragments = DashboardQuery.Address2.Fragments(
                            AddressFragment(
                                street = "Testvägen 1",
                                postalCode = "123 45",
                                city = "Tensta"
                            )
                        )
                    ),
                    numberCoInsured = 2,
                    squareMeters = 50,
                    nhcType = NorwegianHomeContentLineOfBusiness.RENT
                ),
                asSwedishApartmentAgreement = null,
                asNorwegianTravelAgreement = null,
                asSwedishHouseAgreement = null
            ),
            listOf(
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                )
            )
        )

        private val NORWEGIAN_TRAVEL_CONTRACT = Contract(
            "eaaf8b5c-5a61-44a9-91bc-3de5b6bf878e",
            ContractStatus.ACTIVE,
            LocalDate.of(2020, 1, 1),
            null,
            DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    status = AgreementStatus.ACTIVE,
                    activeFrom = LocalDate.of(2020, 1, 1),
                    activeTo = null
                ),
                asNorwegianHomeContentAgreement = null,
                asNorwegianTravelAgreement = DashboardQuery.AsNorwegianTravelAgreement(
                    numberCoInsured = 2
                ),
                asSwedishApartmentAgreement = null,
                asSwedishHouseAgreement = null
            ),
            listOf(
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                ),
                PerilCategoryFragment.Peril(
                    id = "ME.LEGAL",
                    title = "Mock",
                    description = "Mock"
                )
            )
        )

        val NORWEGIAN_HOME_CONTENTS = DashboardData(
            listOf(NORWEGIAN_HOME_CONTENTS_CONTRACT)
        )

        val NORWEGIAN_TRAVEL = DashboardData(
            listOf(NORWEGIAN_TRAVEL_CONTRACT)
        )

        val NORWEGIAN_HOME_CONTENTS_AND_TRAVEL = DashboardData(
            listOf(NORWEGIAN_HOME_CONTENTS_CONTRACT, NORWEGIAN_TRAVEL_CONTRACT)
        )
    }
}
