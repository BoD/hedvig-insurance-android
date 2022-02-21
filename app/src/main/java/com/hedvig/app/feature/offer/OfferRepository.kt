package com.hedvig.app.feature.offer

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.graphql.LastQuoteIdQuery
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.QuoteCartSubscription
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.quotebundle.Campaign
import com.hedvig.app.feature.offer.model.quotebundle.toCampaign
import com.hedvig.app.feature.offer.model.quotebundle.toCheckoutMethod
import com.hedvig.app.feature.offer.model.quotebundle.toIncentive
import com.hedvig.app.feature.offer.model.quotebundle.toQuoteBundle
import com.hedvig.app.feature.offer.ui.checkoutLabel
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeSubscription
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfferRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
    private val featureManager: FeatureManager,
) {
    fun offerQuery(ids: List<String>) = OfferQuery(localeManager.defaultLocale(), ids)

    fun offer(ids: List<String>): Flow<OfferResult> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            val subscription = QuoteCartSubscription(localeManager.defaultLocale(), ids.first())
            apolloClient.subscribe(subscription)
                .safeSubscription()
                .map { it.toDataOrError() }
        } else {
            apolloClient
                .query(offerQuery(ids))
                .watcher()
                .toFlow()
                .map { it.toDataOrError() }
        }
    }

    sealed class OfferResult {
        data class Error(val message: String? = null) : OfferResult()
        data class Success(val data: OfferModel) : OfferResult()
    }

    private fun Response<OfferQuery.Data>.toDataOrError(): OfferResult {
        errors?.let {
            return OfferResult.Error(it.firstOrNull()?.message)
        }
        val data = data ?: return OfferResult.Error()
        val offerResult = OfferModel(
            quoteBundle = data.quoteBundle.fragments.quoteBundleFragment.toQuoteBundle(),
            checkoutMethod = data.signMethodForQuotes.toCheckoutMethod(),
            checkoutLabel = data.checkoutLabel(),
            campaign = Campaign(
                displayValue = data.redeemedCampaigns
                    .firstNotNullOfOrNull { it.fragments.incentiveFragment.displayValue },
                incentive = data.redeemedCampaigns.firstOrNull()?.fragments?.incentiveFragment?.incentive?.toIncentive()
                    ?: Campaign.Incentive.NoDiscount
            )
        )
        return OfferResult.Success(offerResult)
    }

    private fun QueryResult<QuoteCartSubscription.Data>.toDataOrError(): OfferResult {
        return when (this) {
            is QueryResult.Error -> OfferResult.Error(message)
            is QueryResult.Success -> {
                if (data.quoteCart == null) {
                    OfferResult.Error()
                } else {
                    val offerResult = OfferModel(
                        quoteBundle = data.quoteCart!!.bundle!!.fragments.quoteBundleFragment.toQuoteBundle(),
                        checkoutMethod = data.quoteCart!!.checkoutMethods.map { it.toCheckoutMethod() }.first(),
                        checkoutLabel = data.quoteCart!!.checkoutLabel(),
                        campaign = data.quoteCart!!.campaign?.toCampaign()
                    )
                    OfferResult.Success(offerResult)
                }
            }
        }
    }

    fun writeDiscountToCache(ids: List<String>, data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClient
            .apolloStore
            .read(offerQuery(ids))
            .execute()

        val newCost = cachedData.quoteBundle.fragments.copy(
            quoteBundleFragment = cachedData.quoteBundle.fragments.quoteBundleFragment.copy(
                bundleCost = cachedData.quoteBundle.fragments.quoteBundleFragment.bundleCost.copy(
                    fragments = QuoteBundleFragment.BundleCost.Fragments(
                        costFragment = data.redeemCode.cost.fragments.costFragment
                    )
                )
            )
        )

        val newData = cachedData
            .copy(
                quoteBundle = cachedData.quoteBundle.copy(
                    fragments = newCost
                ),
                redeemedCampaigns = listOf(
                    OfferQuery.RedeemedCampaign(
                        fragments = OfferQuery.RedeemedCampaign.Fragments(
                            incentiveFragment = data.redeemCode.campaigns[0].fragments.incentiveFragment
                        )
                    )
                )
            )

        apolloClient
            .apolloStore
            .writeAndPublish(offerQuery(ids), newData)
            .execute()
    }

    suspend fun removeDiscount() = apolloClient
        .mutate(RemoveDiscountCodeMutation())
        .await()

    fun removeDiscountFromCache(ids: List<String>) {
        val cachedData = apolloClient
            .apolloStore
            .read(offerQuery(ids))
            .execute()

        val oldCostFragment = cachedData.quoteBundle.fragments.quoteBundleFragment.bundleCost.fragments.costFragment
        val newCostFragment = oldCostFragment
            .copy(
                monthlyDiscount = oldCostFragment
                    .monthlyDiscount
                    .copy(
                        fragments = CostFragment.MonthlyDiscount.Fragments(
                            oldCostFragment.monthlyDiscount.fragments.monetaryAmountFragment.copy(
                                amount = "0.00"
                            )
                        )
                    ),
                monthlyNet = oldCostFragment
                    .monthlyNet
                    .copy(
                        fragments = CostFragment.MonthlyNet.Fragments(
                            oldCostFragment.monthlyNet.fragments.monetaryAmountFragment.copy(
                                amount = oldCostFragment.monthlyGross.fragments.monetaryAmountFragment.amount
                            )
                        )
                    )
            )

        val newData = cachedData
            .copy(
                quoteBundle = cachedData.quoteBundle.copy(
                    fragments = cachedData.quoteBundle.fragments.copy(
                        quoteBundleFragment = cachedData.quoteBundle.fragments.quoteBundleFragment.copy(
                            bundleCost = QuoteBundleFragment.BundleCost(
                                fragments = QuoteBundleFragment.BundleCost.Fragments(newCostFragment)
                            )
                        )
                    ),
                ),
                redeemedCampaigns = emptyList()
            )

        apolloClient
            .apolloStore
            .writeAndPublish(offerQuery(ids), newData)
            .execute()
    }

    fun quoteIdOfLastQuoteOfMember(): ApolloCall<LastQuoteIdQuery.Data> = apolloClient
        .query(LastQuoteIdQuery())

    fun refreshOfferQuery(ids: List<String>) = apolloClient
        .query(offerQuery(ids))
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
}
