package com.example.sr13

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * BaseActivity serves as a common base class for all activities in the app.
 * It provides utility functions for showing a progress dialog and snackbars
 * to handle common UI interactions such as loading indicators and error messages.
 */
open class BaseActivity : AppCompatActivity() {
    private var mProgressDialog: ProgressDialog? = null


    /**
     * Displays a progress dialog with the specified text.
     *
     * @param text The message to be displayed in the progress dialog.
     */
    fun showProgressDialog(text: String) {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage(text)
        mProgressDialog!!.show()
    }

    /**
     * Hides the currently displayed progress dialog, if any.
     */
    fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    /**
     * Displays a Snackbar with a custom message.
     * The Snackbar's background color changes based on the type of message (error or success).
     *
     * @param message The message to be displayed in the Snackbar.
     * @param errorMessage A boolean indicating if the message is an error message.
     *                      If true, the Snackbar background will be styled as an error.
     */
    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        if (errorMessage) {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    R.color.colorSnackBarError
                )
            )
        } else {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    R.color.colorSnackBarSuccess
                )
            )
        }
        snackbar.show()
    }
}
