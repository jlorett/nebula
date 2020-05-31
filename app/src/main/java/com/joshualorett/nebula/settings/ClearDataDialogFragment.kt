package com.joshualorett.nebula.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joshualorett.nebula.R

/**
 * Displays a dialog that allows users to clear app data.
 * Created by Joshua on 5/31/2020.
 */
class ClearDataDialogFragment: DialogFragment() {
    private var listener: Listener? = null

    interface Listener {
        fun onClear()
    }

    companion object {
        @JvmStatic
        fun create() : ClearDataDialogFragment {
            return ClearDataDialogFragment()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return context?.let {
            buildDialog(it)
        } ?: throw IllegalStateException("Activity can't be null.")
    }

    override fun onDestroy() {
        listener = null
        super.onDestroy()
    }

    private fun buildDialog(context: Context) : Dialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.clear_data))
            .setMessage(getString(R.string.clear_data_summary))
            .setPositiveButton(getString(R.string.clear)) { dialog, _ ->
                listener?.onClear()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
            .create()
    }
}