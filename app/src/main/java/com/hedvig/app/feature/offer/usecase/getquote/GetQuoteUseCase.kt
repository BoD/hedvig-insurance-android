package com.hedvig.app.feature.offer.usecase.getquote

import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import kotlinx.coroutines.flow.first

class GetQuoteUseCase(
    private val offerRepository: OfferRepository,
) {
    sealed class Result {
        data class Success(
            val quote: QuoteBundle.Quote,
        ) : Result()

        object Error : Result()
    }

    suspend operator fun invoke(bundleIds: List<String>, quoteId: String): Result {
        val offer = offerRepository
            .offer(bundleIds)
            .first()

        if (offer !is OfferRepository.OfferResult.Success) {
            return Result.Error
        }
        val quote = offer.data.quoteBundle.quotes.firstOrNull { it.id == quoteId } ?: return Result.Error
        return Result.Success(quote)
    }
}