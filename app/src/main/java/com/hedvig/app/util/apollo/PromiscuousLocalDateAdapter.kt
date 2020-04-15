package com.hedvig.app.util.apollo

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import e
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException

class PromiscuousLocalDateAdapter : CustomTypeAdapter<LocalDate> {
    override fun encode(value: LocalDate): CustomTypeValue<*> =
        CustomTypeValue.fromRawValue(value.toString())

    override fun decode(value: CustomTypeValue<*>): LocalDate? = try {
        LocalDate.parse((value.value as String).substring(0, 10))
    } catch (e: DateTimeParseException) {
        e(e)
        null
    }
}

