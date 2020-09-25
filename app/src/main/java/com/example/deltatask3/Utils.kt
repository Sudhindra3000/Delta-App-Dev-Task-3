package com.example.deltatask3

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showSnackbar(view: View, string: String, duration: Int) {
    Snackbar.make(view, string, duration).show()
}