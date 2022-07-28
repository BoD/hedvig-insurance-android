package com.hedvig.app.util.extensions

import androidx.lifecycle.MutableLiveData

/**
 * Takes current value and updates it with the result of [function] if current value is not null
 */
fun <T> MutableLiveData<T>.update(function: (T) -> T) {
  val prevValue = value
  if (prevValue != null) {
    val nextValue = function(prevValue)
    postValue(nextValue)
  }
}
