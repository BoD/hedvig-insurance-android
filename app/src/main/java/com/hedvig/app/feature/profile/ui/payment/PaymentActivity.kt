package com.hedvig.app.feature.profile.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityPaymentBinding
import com.hedvig.app.databinding.AdyenPayinDetailsBinding
import com.hedvig.app.databinding.CampaignInformationSectionBinding
import com.hedvig.app.databinding.ConnectBankAccountCardBinding
import com.hedvig.app.databinding.FailedPaymentsCardBinding
import com.hedvig.app.databinding.NextPaymentCardBinding
import com.hedvig.app.databinding.PaymentHistoryItemBinding
import com.hedvig.app.databinding.PaymentHistoryLinkBinding
import com.hedvig.app.databinding.PaymentLinkBinding
import com.hedvig.app.databinding.TrustlyPayinDetailsBinding
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.referrals.ui.redeemcode.RefetchingRedeemCodeDialog
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PaymentActivity : BaseActivity(R.layout.activity_payment) {
    private val binding by viewBinding(ActivityPaymentBinding::bind)
    private val model: PaymentViewModel by viewModel()

    private val tracker: PaymentTracker by inject()
    private val marketProvider: MarketProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val market = getMarket()
        if (market == null) {
            startActivity(MarketingActivity.newInstance(this))
        }
        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)

            setupToolbar(R.id.toolbar, R.drawable.ic_back, true, root) {
                onBackPressed()
            }

            recycler.adapter = PaymentAdapter(marketProvider, supportFragmentManager)

            model.data.observe(this@PaymentActivity) { (paymentData, payinStatusData) ->
                if (paymentData == null || payinStatusData == null) {
                    return@observe
                }

                (recycler.adapter as? PaymentAdapter)?.submitList(
                    listOfNotNull(
                        safeLet(
                            paymentData.balance.failedCharges,
                            paymentData.nextChargeDate
                        ) { failedCharges, nextChargeDate ->
                            if (failedCharges > 0) {
                                PaymentModel.FailedPayments(failedCharges, nextChargeDate)
                            } else {
                                null
                            }
                        },
                        PaymentModel.NextPayment(paymentData),
                        if (payinStatusData.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
                            PaymentModel.ConnectPayment
                        } else {
                            null
                        }
                    )
                )
            }
        }
    }

    companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd, LLL yyyy")

        fun isActive(contracts: List<PaymentQuery.Contract>) = contracts.any {
            it.status.fragments.contractStatusFragment.asActiveStatus != null
                || it.status.fragments.contractStatusFragment.asTerminatedInFutureStatus != null
                || it.status.fragments.contractStatusFragment.asTerminatedTodayStatus != null
        }

        fun isPending(contracts: List<PaymentQuery.Contract>) = contracts.all {
            it.status.fragments.contractStatusFragment.asPendingStatus != null
                || it.status.fragments.contractStatusFragment.asActiveInFutureStatus != null
                || it.status.fragments.contractStatusFragment.asActiveInFutureAndTerminatedInFutureStatus != null
        }

        fun newInstance(context: Context) = Intent(context, PaymentActivity::class.java)
    }
}

class PaymentAdapter(
    private val marketProvider: MarketProvider,
    private val fragmentManager: FragmentManager
) :
    ListAdapter<PaymentModel, PaymentAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        PaymentModel.Header -> R.layout.payment_header
        is PaymentModel.FailedPayments -> R.layout.failed_payments_card
        is PaymentModel.NextPayment -> R.layout.next_payment_card
        PaymentModel.ConnectPayment -> R.layout.connect_bank_account_card
        is PaymentModel.CampaignInformation -> R.layout.campaign_information_section
        PaymentModel.PaymentHistoryHeader -> R.layout.payment_history_header
        is PaymentModel.Charge -> R.layout.payment_history_item
        PaymentModel.PaymentHistoryLink -> R.layout.payment_history_link
        is PaymentModel.TrustlyPayinDetails -> R.layout.trustly_payin_details
        is PaymentModel.AdyenPayinDetails -> R.layout.adyen_payin_details
        is PaymentModel.Link -> R.layout.payment_link
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.payment_header -> ViewHolder.Header(parent)
        R.layout.failed_payments_card -> ViewHolder.FailedPayments(parent)
        R.layout.next_payment_card -> ViewHolder.NextPayment(parent)
        R.layout.connect_bank_account_card -> ViewHolder.ConnectPayment(parent)
        R.layout.campaign_information_section -> ViewHolder.CampaignInformation(parent)
        R.layout.payment_history_header -> ViewHolder.PaymentHistoryHeader(parent)
        R.layout.payment_history_item -> ViewHolder.Charge(parent)
        R.layout.payment_history_link -> ViewHolder.PaymentHistoryLink(parent)
        R.layout.trustly_payin_details -> ViewHolder.TrustlyPayinDetails(parent)
        R.layout.adyen_payin_details -> ViewHolder.AdyenPayinDetails(parent)
        R.layout.payment_link -> ViewHolder.Link(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), marketProvider, fragmentManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: PaymentModel,
            marketProvider: MarketProvider,
            fragmentManager: FragmentManager
        ): Any?

        fun invalid(data: PaymentModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_header)) {
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = Unit
        }

        class FailedPayments(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.failed_payments_card)) {
            private val binding by viewBinding(FailedPaymentsCardBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is PaymentModel.FailedPayments) {
                    return invalid(data)
                }

                failedPaymentsParagraph.text = failedPaymentsParagraph.context.getString(
                    R.string.PAYMENTS_LATE_PAYMENTS_MESSAGE,
                    data.failedCharges,
                    data.nextChargeDate
                )
            }
        }

        class NextPayment(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.next_payment_card)) {
            private val binding by viewBinding(NextPaymentCardBinding::bind)

            init {
                binding.nextPaymentGross.setStrikethrough(true)
            }

            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is PaymentModel.NextPayment) {
                    return invalid(data)
                }

                // nextPaymentAmount.text = nextPaymentAmount.context.getString(
                //     R.string.PAYMENTS_CURRENT_PREMIUM,
                //     data.inner.chargeEstimation.charge.amount.toBigDecimal().toInt()
                // )

                // val discount = data.inner.chargeEstimation.discount.amount.toBigDecimal().toInt()
                // if (discount > 0 && data.inner.balance.failedCharges == 0) {
                //     nextPaymentGross.show()
                //     nextPaymentGross.text = nextPaymentGross.context.getString(
                //         R.string.PAYMENTS_FULL_PREMIUM,
                //         data.inner.insuranceCost?.fragments?.costFragment?.monthlyGross?.fragments?.monetaryAmountFragment?.amount?.toBigDecimal()
                //             ?.toInt()
                //     )
                // }

                if (PaymentActivity.isActive(data.inner.contracts)) {
                    nextPaymentDate.text =
                        data.inner.nextChargeDate?.format(PaymentActivity.DATE_FORMAT)
                } else if (PaymentActivity.isPending(data.inner.contracts)) {
                    nextPaymentDate.background.compatSetTint(nextPaymentDate.context.compatColor(R.color.sunflower_300))
                    nextPaymentDate.setTextColor(nextPaymentDate.context.compatColor(R.color.off_black))
                    nextPaymentDate.setText(R.string.PAYMENTS_CARD_NO_STARTDATE)
                }

                val incentive =
                    data.inner.redeemedCampaigns.getOrNull(0)?.fragments?.incentiveFragment?.incentive
                incentive?.asFreeMonths?.let { freeMonthsIncentive ->
                    freeMonthsIncentive.quantity?.let { quantity ->
                        discountSphereText.text = buildSpannedString {
                            scale(20f / 12f) {
                                append("$quantity\n")
                            }
                            append(
                                if (quantity > 1) {
                                    discountSphere.context.getString(R.string.PAYMENTS_OFFER_MULTIPLE_MONTHS)
                                } else {
                                    discountSphere.context.getString(R.string.PAYMENTS_OFFER_SINGLE_MONTH)
                                }
                            )
                        }
                        discountSphere.show()
                    }
                }
                incentive?.asPercentageDiscountMonths?.let { percentageDiscountMonthsIncentive ->
                    discountSphere.show()
                    discountSphereText.text =
                        if (percentageDiscountMonthsIncentive.pdmQuantity > 1) {
                            discountSphereText.context.getString(
                                R.string.PAYMENTS_DISCOUNT_PERCENTAGE_MONTHS_MANY,
                                percentageDiscountMonthsIncentive.percentageDiscount.toInt(),
                                percentageDiscountMonthsIncentive.pdmQuantity
                            )
                        } else {
                            discountSphere.context.getString(
                                R.string.PAYMENTS_DISCOUNT_PERCENTAGE_MONTHS_ONE,
                                percentageDiscountMonthsIncentive.percentageDiscount.toInt()
                            )
                        }
                }
            }
        }

        class ConnectPayment(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.connect_bank_account_card)) {
            private val binding by viewBinding(ConnectBankAccountCardBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                connectBankAccount.setHapticClickListener {
                    marketProvider.market?.connectPayin(connectBankAccount.context)
                        ?.let { connectBankAccount.context.startActivity(it) }
                }
            }
        }

        class CampaignInformation(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.campaign_information_section)) {
            private val binding by viewBinding(CampaignInformationSectionBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is PaymentModel.CampaignInformation) {
                    return invalid(data)
                }

                val incentive =
                    data.inner.redeemedCampaigns.getOrNull(0)?.fragments?.incentiveFragment?.incentive
                incentive?.asFreeMonths?.let {
                    campaignInformationTitle.setText(R.string.PAYMENTS_SUBTITLE_CAMPAIGN)
                    campaignInformationLabelOne.setText(R.string.PAYMENTS_CAMPAIGN_OWNER)
                    data.inner.redeemedCampaigns.getOrNull(0)?.owner?.displayName?.let { displayName ->
                        campaignInformationFieldOne.text = displayName
                    }

                    when {
                        PaymentActivity.isActive(data.inner.contracts) -> {
                            data.inner.insuranceCost?.freeUntil?.let { freeUntil ->
                                lastFreeDay.text = freeUntil.format(PaymentActivity.DATE_FORMAT)
                            }
                            lastFreeDay.show()
                            lastFreeDayLabel.show()
                        }
                        PaymentActivity.isPending(data.inner.contracts) -> {
                            willUpdateWhenStartDateIsSet.show()
                        }
                        else -> {
                        }
                    }
                }
                incentive?.asMonthlyCostDeduction?.let { monthlyCostDeductionIncentive ->
                    campaignInformationTitle.setText(R.string.PAYMENTS_SUBTITLE_DISCOUNT)
                    campaignInformationLabelOne.setText(R.string.PAYMENTS_DISCOUNT_ZERO)
                    monthlyCostDeductionIncentive.amount?.amount?.toBigDecimal()?.toInt()
                        ?.toString()
                        ?.let { amount ->
                            campaignInformationFieldOne.text =
                                campaignInformationFieldOne.context.getString(
                                    R.string.PAYMENTS_DISCOUNT_AMOUNT,
                                    amount
                                )
                        }
                }
            }
        }

        class PaymentHistoryHeader(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payment_history_header)) {
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = Unit
        }

        class Charge(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payment_history_item)) {
            private val binding by viewBinding(PaymentHistoryItemBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is PaymentModel.Charge) {
                    return invalid(data)
                }

                date.text = data.inner.date.format(PaymentActivity.DATE_FORMAT)
                amount.text = data.inner.amount.fragments.monetaryAmountFragment.toMonetaryAmount()
                    .format(amount.context)
            }
        }

        class PaymentHistoryLink(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payment_history_link)) {
            private val binding by viewBinding(PaymentHistoryLinkBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                root.setHapticClickListener {
                    root.context.startActivity(PaymentHistoryActivity.newInstance(root.context))
                }
            }
        }

        class TrustlyPayinDetails(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.trustly_payin_details)) {
            private val binding by viewBinding(TrustlyPayinDetailsBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is PaymentModel.TrustlyPayinDetails) {
                    return invalid(data)
                }

                accountNumber.text =
                    "${data.bankAccount.fragments.bankAccountFragment.bankName} ${data.bankAccount.fragments.bankAccountFragment.descriptor}"
                when (data.status) {
                    PayinMethodStatus.ACTIVE -> directDebitStatus.setText(R.string.PAYMENTS_DIRECT_DEBIT_ACTIVE)
                    PayinMethodStatus.PENDING -> directDebitStatus.setText(R.string.PAYMENTS_DIRECT_DEBIT_PENDING)
                    PayinMethodStatus.NEEDS_SETUP -> directDebitStatus.setText(R.string.PAYMENTS_DIRECT_DEBIT_NEEDS_SETUP)
                    else -> {
                    }
                }
                bankAccountUnderChangeParagraph.isVisible = data.status == PayinMethodStatus.PENDING
            }
        }

        class AdyenPayinDetails(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.adyen_payin_details)) {
            private val binding by viewBinding(AdyenPayinDetailsBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is PaymentModel.AdyenPayinDetails) {
                    return invalid(data)
                }

                cardType.text =
                    data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.brand
                maskedCardNumber.text =
                    "**** ${data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.lastFourDigits}"
                validUntil.text =
                    "${data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryMonth}/${data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryYear}"
            }
        }

        class Link(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_link)) {
            private val binding by viewBinding(PaymentLinkBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is PaymentModel.Link) {
                    return invalid(data)
                }

                root.setText(
                    when (data) {
                        PaymentModel.Link.TrustlyChangePayin -> R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT
                        PaymentModel.Link.AdyenChangePayin -> R.string.MY_PAYMENT_CHANGE_CREDIT_CARD_BUTTON
                        PaymentModel.Link.RedeemDiscountCode -> R.string.REFERRAL_ADDCOUPON_HEADLINE
                    }
                )

                root.setHapticClickListener {
                    when (data) {
                        PaymentModel.Link.TrustlyChangePayin,
                        PaymentModel.Link.AdyenChangePayin -> marketProvider.market?.connectPayin(
                            root.context
                        )?.let { root.context.startActivity(it) }
                        PaymentModel.Link.RedeemDiscountCode -> RefetchingRedeemCodeDialog
                            .newInstance()
                            .show(fragmentManager, RefetchingRedeemCodeDialog.TAG)
                    }
                }
            }
        }
    }
}

sealed class PaymentModel {
    object Header : PaymentModel()
    data class FailedPayments(
        val failedCharges: Int,
        val nextChargeDate: LocalDate
    ) : PaymentModel()

    data class NextPayment(
        val inner: PaymentQuery.Data
    ) : PaymentModel()

    object ConnectPayment : PaymentModel()

    data class CampaignInformation(val inner: PaymentQuery.Data) : PaymentModel()

    object PaymentHistoryHeader : PaymentModel()
    data class Charge(val inner: ProfileQuery.ChargeHistory) : PaymentModel()
    object PaymentHistoryLink : PaymentModel()

    data class TrustlyPayinDetails(
        val bankAccount: ProfileQuery.BankAccount,
        val status: PayinMethodStatus
    ) : PaymentModel()

    data class AdyenPayinDetails(val inner: ProfileQuery.ActivePaymentMethods) : PaymentModel()

    sealed class Link : PaymentModel() {
        object TrustlyChangePayin : Link()
        object AdyenChangePayin : Link()
        object RedeemDiscountCode : Link()
    }
}
