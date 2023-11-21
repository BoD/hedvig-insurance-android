package com.hedvig.android.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * A formatter which emulates the [DateTimeFormatter.ISO_LOCAL_DATE] but replaces the '-' dashes with '.' dots.
 * Example output: "2021.07.01"
 */
fun hedvigDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .appendLiteral('.')
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral('.')
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .toFormatter(locale)
}

/**
 * Example output: "May 26 2023"
 */
fun hedvigSecondaryDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatter.ofPattern("MMMM d yyyy", locale)
}

@Composable
fun rememberHedvigDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigDateTimeFormatter(locale) }
}

// todo, migrate the above into [HedvigDateTimeFormatterDefaults]
object HedvigDateTimeFormatterDefaults {
  fun isoLocalDateWithDots(locale: Locale): DateTimeFormatter {
    return isoLocalDateWithDots.toFormatter(locale)
  }

  fun timeOnly(locale: Locale): DateTimeFormatter {
    return timeOnly.toFormatter(locale)
  }

  fun dayOfTheWeekAndTime(locale: Locale): DateTimeFormatter {
    return dayOfTheWeekAndTime.toFormatter(locale)
  }

  fun monthDateAndTime(locale: Locale): DateTimeFormatter {
    return monthDateAndTime.toFormatter(locale)
  }

  fun yearMonthDateAndTime(locale: Locale): DateTimeFormatter {
    return yearMonthDateAndTime.toFormatter(locale)
  }
}

/**
 * Example output: "2021.07.01"
 */
private val isoLocalDateWithDots: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
  .appendLiteral('.')
  .appendValue(ChronoField.MONTH_OF_YEAR, 2)
  .appendLiteral('.')
  .appendValue(ChronoField.DAY_OF_MONTH, 2)

/**
 * Example output: "12:34"
 */
private val timeOnly: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)

/**
 * Example output: "Fri 12:34"
 */
private val dayOfTheWeekAndTime: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendPattern("EEE")
  .appendLiteral(' ')
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)

/**
 * Example output: "Nov 11 9:04"
 */
private val monthDateAndTime: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendPattern("MMM")
  .appendLiteral(' ')
  .appendValue(ChronoField.DAY_OF_MONTH, 2)
  .appendLiteral(' ')
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)

/**
 * Example output: "2022 Nov 11 9:04"
 */
private val yearMonthDateAndTime: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendPattern("yyyy")
  .appendLiteral(' ')
  .appendPattern("MMM")
  .appendLiteral(' ')
  .appendValue(ChronoField.DAY_OF_MONTH, 2)
  .appendLiteral(' ')
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
