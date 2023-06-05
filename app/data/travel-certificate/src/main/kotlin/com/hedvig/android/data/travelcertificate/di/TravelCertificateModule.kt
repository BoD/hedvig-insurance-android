package com.hedvig.android.data.travelcertificate.di

import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import org.koin.dsl.module

val travelCertificateDataModule = module {
  single<GetTravelCertificateSpecificationsUseCase> {
    GetTravelCertificateSpecificationsUseCase(get(octopusClient), get<FeatureManager>())
  }
}
