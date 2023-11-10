package com.hedvig.android.data.payment

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode
import org.javamoney.moneta.Money

class PaymentRepositoryDemo : PaymentRepository {
  override suspend fun getChargeHistory(): Either<ErrorMessage, ChargeHistory> = either {
    ChargeHistory(
      List(5) { index ->
        ChargeHistory.Charge(
          amount = UiMoney(49.0 * (index + 1), CurrencyCode.SEK),
          date = Clock.System.now().minus(31.days * index).toLocalDateTime(TimeZone.UTC).date,
          paymentStatus = ChargeHistory.Charge.PaymentStatus.SUCCESSFUL,
        )
      },
    )
  }

  override suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData> = either {
    PaymentData(
      nextCharge = Money.of(BigDecimal(100), "SEK"),
      monthlyCost = Money.of(BigDecimal(100), "SEK"),
      totalDiscount = Money.of(BigDecimal(10), "SEK"),
      nextChargeDate = LocalDate.now(),
      redeemedCampagins = listOf(),
      bankName = "Test Bank",
      bankDescriptor = "123456",
      paymentMethod = null,
      bankAccount = null,
      contracts = listOf(),
      payoutMethodStatus = null,
    )
  }
}