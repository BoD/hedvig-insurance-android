package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generate.CoInsured
import com.hedvig.android.feature.travelcertificate.ui.generate_when.TravelCertificatePrimaryInput
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface TravelCertificateDestination : Destination {
  @Serializable
  data object TravelCertificateHistory : TravelCertificateDestination

  @Serializable
  data object TravelCertificateChooseContract : TravelCertificateDestination

  @Serializable
  data class GenerateTravelCertificateDestinations(
    val contractId: String?,
  ) : TravelCertificateDestination

  @Serializable
  data class TravelCertificateInput(
    val contractId: String?,
  ) : TravelCertificateDestination

  @Serializable
  data class TravelCertificateDateInput(
    val contractId: String?,
  ) : TravelCertificateDestination

  @Serializable
  data class TravelCertificateTravellersInput(
    val primaryInput: TravelCertificatePrimaryInput,
  ) : TravelCertificateDestination

  @Serializable
  data class AddCoInsured(
    val coInsured: CoInsured?,
    val contractId: String?,
  ) : TravelCertificateDestination

  @Serializable
  data class ShowCertificate(
    val travelCertificateUrl: TravelCertificateUrl,
  ) : TravelCertificateDestination
}
