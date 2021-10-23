package com.example.deltatask3

import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

suspend fun showSnackbarInMain(view: View, string: String, duration: Int) {
    withContext(Dispatchers.Main) {
        Snackbar.make(view, string, duration).show()
    }
}

fun firstLetterToUppercase(string: String) =
    string.substring(0, 1).uppercase(Locale.ROOT) + string.substring(1)
