package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.CoInsured
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface TravelCertificateDestination : Destination {
  @Serializable
  data object TravelCertificateHistory : TravelCertificateDestination

  @Serializable
  data object GenerateTravelCertificateDestinations : TravelCertificateDestination

  @Serializable
  data object TravelCertificateInput : TravelCertificateDestination

  @Serializable
  data class AddCoInsured(
    val coInsured: CoInsured?,
  ) : TravelCertificateDestination

  @Serializable
  data class ShowCertificate(
    val travelCertificateUrl: TravelCertificateUrl,
  ) : TravelCertificateDestination
}