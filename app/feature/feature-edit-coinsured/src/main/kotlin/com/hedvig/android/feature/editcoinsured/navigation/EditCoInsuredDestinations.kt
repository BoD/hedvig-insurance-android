package com.hedvig.android.feature.editcoinsured.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface EditCoInsuredDestination : Destination {
  @Serializable
  data class AddOrRemove(
    val contractId: String,
  ) : Destination

  @Serializable
  data class Success(
    val date: LocalDate,
  ) : Destination
}
