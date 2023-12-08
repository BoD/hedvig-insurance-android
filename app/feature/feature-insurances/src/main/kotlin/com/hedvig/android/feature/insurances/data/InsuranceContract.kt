package com.hedvig.android.feature.insurances.data

import com.hedvig.android.data.productvariant.ProductVariant
import java.time.format.DateTimeFormatter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

data class InsuranceContract(
  val id: String,
  val displayName: String,
  val contractHolderDisplayName: String,
  val contractHolderSSN: String?,
  val exposureDisplayName: String,
  val inceptionDate: LocalDate,
  val terminationDate: LocalDate?,
  val currentInsuranceAgreement: InsuranceAgreement,
  val upcomingInsuranceAgreement: InsuranceAgreement?,
  val renewalDate: LocalDate?,
  val supportsAddressChange: Boolean,
  val supportsEditCoInsured: Boolean,
  val isTerminated: Boolean,
)

data class InsuranceAgreement(
  val activeFrom: LocalDate,
  val activeTo: LocalDate,
  val displayItems: List<DisplayItem>,
  val productVariant: ProductVariant,
  val certificateUrl: String?,
  val coInsured: ImmutableList<CoInsured>,
  val creationCause: CreationCause,
) {
  data class DisplayItem(
    val title: String,
    val value: String,
  )

  data class CoInsured(
    private val ssn: String?,
    private val birthDate: LocalDate?,
    private val firstName: String?,
    private val lastName: String?,
    val activatesOn: LocalDate?,
    val terminatesOn: LocalDate?,
    val hasMissingInfo: Boolean,
  ) {
    fun getDisplayName() = buildString {
      if (firstName != null) {
        append(firstName)
      }
      if (firstName != null && lastName != null) {
        append(" ")
      }
      if (lastName != null) {
        append(lastName)
      }
    }

    fun getSsnOrBirthDate(dateTimeFormatter: DateTimeFormatter) = ssn ?: birthDate
      ?.toJavaLocalDate()
      ?.let { dateTimeFormatter.format(it) }
  }

  enum class CreationCause {
    UNKNOWN,
    NEW_CONTRACT,
    RENEWAL,
    MIDTERM_CHANGE,
  }
}
