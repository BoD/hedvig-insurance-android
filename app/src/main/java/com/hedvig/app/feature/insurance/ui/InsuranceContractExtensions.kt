package com.hedvig.app.feature.insurance.ui

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import e
import java.time.format.DateTimeFormatter

fun InsuranceQuery.Contract.bindTo(binding: InsuranceContractCardBinding) =
binding.apply {
        status.fragments.contractStatusFragment.let { contractStatus ->
            contractStatus.asPendingStatus?.let {
                firstStatusPill.show()
                firstStatusPill.setText(R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_NO_STARTDATE)
            }
            contractStatus.asActiveInFutureStatus?.let { activeInFuture ->
                firstStatusPill.show()
                firstStatusPill.text = firstStatusPill.resources.getString(
                    R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE,
                    dateTimeFormatter.format(activeInFuture.futureInception)
                )
            }
            contractStatus.asActiveInFutureAndTerminatedInFutureStatus?.let { activeAndTerminated ->
                firstStatusPill.show()
                firstStatusPill.text = firstStatusPill.resources.getString(
                    R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE,
                    dateTimeFormatter.format(activeAndTerminated.futureInception)
                )
                secondStatusPill.show()
                secondStatusPill.text = secondStatusPill.context.getString(
                    R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_TERMINATIONDATE,
                    dateTimeFormatter.format(activeAndTerminated.futureTermination)
                )
            }
            contractStatus.asTerminatedInFutureStatus?.let { terminatedInFuture ->
                firstStatusPill.show()
                firstStatusPill.text = firstStatusPill.resources.getString(
                    R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_TERMINATIONDATE,
                    dateTimeFormatter.format(terminatedInFuture.futureTermination)
                )
            }
            contractStatus.asTerminatedTodayStatus?.let {
                firstStatusPill.show()
                firstStatusPill.setText(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED_TODAY)
            }
            contractStatus.asTerminatedStatus?.let {
                firstStatusPill.show()
                firstStatusPill.setText(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED)
            }
            contractStatus.asActiveStatus?.let {
                when (typeOfContract) {
                    TypeOfContract.SE_HOUSE,
                    TypeOfContract.SE_APARTMENT_BRF,
                    TypeOfContract.SE_APARTMENT_RENT,
                    TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                    TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                    TypeOfContract.NO_HOME_CONTENT_OWN,
                    TypeOfContract.NO_HOME_CONTENT_RENT,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> {
                        container.setBackgroundResource(R.drawable.card_home_background)
                    }
                    TypeOfContract.NO_TRAVEL,
                    TypeOfContract.NO_TRAVEL_YOUTH,
                    TypeOfContract.DK_HOME_CONTENT -> {
                        container.setBackgroundResource(R.drawable.card_travel_background)
                    }
                    TypeOfContract.UNKNOWN__ -> {

                    }
                }
            } ?: run {
                container.setBackgroundResource(R.color.hedvig_light_gray)
            }
        }


        contractName.text = displayName

        contractPills.adapter = ContractPillAdapter().also { adapter ->
            when (typeOfContract) {
                TypeOfContract.SE_HOUSE,
                TypeOfContract.SE_APARTMENT_BRF,
                TypeOfContract.SE_APARTMENT_RENT,
                TypeOfContract.NO_HOME_CONTENT_OWN,
                TypeOfContract.NO_HOME_CONTENT_RENT,
                TypeOfContract.DK_HOME_CONTENT,
                TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                    adapter.submitList(
                        listOf(
                            ContractModel.Address(currentAgreement),
                            ContractModel.NoOfCoInsured(currentAgreement.numberCoInsured)
                        )
                    )
                }
                TypeOfContract.NO_TRAVEL,
                TypeOfContract.NO_TRAVEL_YOUTH -> {
                    adapter.submitList(listOf(ContractModel.NoOfCoInsured(currentAgreement.numberCoInsured)))
                }
                TypeOfContract.UNKNOWN__ -> {
                }
            }
        }
        root.setHapticClickListener {
            root.transitionName = TRANSITION_NAME
            root.context.startActivity(
                ContractDetailActivity.newInstance(
                    root.context,
                    id
                )
            )
        }
    }

private const val TRANSITION_NAME = "contract_card"

private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu")

private val InsuranceQuery.CurrentAgreement.numberCoInsured: Int
    get() {
        asNorwegianTravelAgreement?.numberCoInsured?.let { return it }
        asSwedishHouseAgreement?.numberCoInsured?.let { return it }
        asSwedishApartmentAgreement?.numberCoInsured?.let { return it }
        asNorwegianHomeContentAgreement?.numberCoInsured?.let { return it }
        e { "Unable to infer amount coinsured for agreement: $this" }
        return 0
    }
