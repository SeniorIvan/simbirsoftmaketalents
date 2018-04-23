package ivan.simbirsoft.maketalents.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

/**
 * Created by Ivan Kuznetsov
on 24.10.2017.
 */

/**
 * dialog with ok button, title(optional) and message
 */

class SimpleMessageDialog : DialogFragment() {

    interface Callback {
        fun onMessageDialogDismissed(tag: String)
    }

    companion object {
        const val TAG = "SimpleMessageDialog"

        private const val TITLE_PARAM = "title_param"
        private const val MESSAGE_PARAM = "message_param"

        fun createInstance(title: String? = null, message: String): SimpleMessageDialog {
            val params = Bundle()
            params.putString(TITLE_PARAM, title)
            params.putString(MESSAGE_PARAM, message)

            val fragment = SimpleMessageDialog()
            fragment.arguments = params

            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //todo !!
        val builder = AlertDialog.Builder(context!!)

        val title: String? = getTitle()
        val message: String? = getMessage()

        // TODO setTitle в принципе может и нуллы принимать
        title?.let {
            builder.setTitle(it)
        }

        builder.setMessage(message)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.cancel()
        }

        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (isCancelable) {
            val parentFragment = parentFragment

            if (parentFragment is Callback) {
                //todo !!
                parentFragment.onMessageDialogDismissed(tag!!)
            }
        }
    }

    private fun getTitle(): String? = arguments?.getString(TITLE_PARAM, null)

    //todo !!
    private fun getMessage(): String  = arguments!!.getString(MESSAGE_PARAM)
}