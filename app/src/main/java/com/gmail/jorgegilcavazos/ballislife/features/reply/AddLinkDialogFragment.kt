package com.gmail.jorgegilcavazos.ballislife.features.reply

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.EditText
import com.gmail.jorgegilcavazos.ballislife.R

/** Dialog fragment that allows adding a link to a string of text. */
class AddLinkDialogFragment : DialogFragment() {

    private var listener: OnFragmentInteractionListener? = null

    /** Interface used by the owner activity to communicate with this fragment. */
    interface OnFragmentInteractionListener {
        fun onLinkAdded(text: String, link: String)
    }

    companion object {
        private const val ARG_TEXT = "Text"

        fun newInstance(text: String = ""): AddLinkDialogFragment {
            val fragment = AddLinkDialogFragment()
            val args = Bundle()
            args.putString(ARG_TEXT, text)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val text = arguments?.getString(ARG_TEXT)

        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_link_dialog, null)
        val editText: EditText = view.findViewById(R.id.edit_text)
        val editLink: EditText = view.findViewById(R.id.edit_link)
        editText.setText(text)

        return AlertDialog.Builder(activity)
                .setTitle(R.string.add_link)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        { _, _ ->
                            listener?.onLinkAdded(editText.text.toString(),
                                    editLink.text.toString())
                            dismiss()
                        })
                .setNegativeButton(R.string.cancel, { _, _ -> dismiss() })
                .create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            val ctx = context.toString()
            throw RuntimeException("$ctx must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
