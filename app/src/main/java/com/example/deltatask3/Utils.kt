package com.example.deltatask3

import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.util.*

fun showSnackbar(view: View, string: String, duration: Int) {
    Snackbar.make(view, string, duration).show()
}

fun firstLetterToUppercase(string: String) = string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1)