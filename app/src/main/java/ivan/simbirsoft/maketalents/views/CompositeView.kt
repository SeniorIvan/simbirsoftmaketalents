package ivan.simbirsoft.maketalents.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ivan.simbirsoft.maketalents.R

/**
 * Created by Ivan Kuznetsov
 * on 25.12.2017.
 */

/**
 * @attr R.styleable.CompositeView_android_layout
 * @attr R.styleable.CompositeView_message_text
 * @attr R.styleable.CompositeView_message_text
 */
class CompositeView : LinearLayout {

    private val mMessageTextView: TextView
    private val mButton: Button

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.CompositeView, defStyleAttr, 0)

        val layoutResId = a.getResourceId(R.styleable.CompositeView_android_layout, NO_ID)
        val messageText = a.getString(R.styleable.CompositeView_message_text)
        val buttonText = a.getString(R.styleable.CompositeView_button_text)
        a.recycle()

        assert(layoutResId == View.NO_ID, { "android:layout is missing" })

        inflate(context, layoutResId, this)

        mMessageTextView = findViewById(R.id.message_text_view)
        mButton = findViewById(R.id.action_button)

        setMessageText(messageText)
        setButtonText(buttonText)

        assert(mMessageTextView != null, { "R.id.message_text_view is missing" })
        assert(mButton != null, { "R.id.action_button is missing" })
    }

    fun setMessageText(text: String) {
        mMessageTextView.text = text
    }

    fun setButtonText(text: String) {
        mButton.text = text
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        mButton.setOnClickListener {
            listener.invoke()
        }
    }
}