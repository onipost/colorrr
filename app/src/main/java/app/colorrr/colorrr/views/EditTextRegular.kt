package app.colorrr.colorrr.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.Typefaces

class EditTextRegular : AppCompatEditText {
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        this.setFont(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        for (i in 0 until attrs.attributeCount) {
            if (attrs.getAttributeName(i) == "text")
                this.setText(
                    attrs.getAttributeResourceValue(
                        i,
                        R.string.Error
                    )
                )
        }
        this.setFont(context)
    }

    constructor(context: Context) : super(context) {
        this.setFont(context)
    }

    private fun setFont(context: Context) {
        val face = Typefaces[context, "fonts/GillSans.ttf"]
        typeface = face
    }
}

