package app.colorrr.colorrr.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import app.colorrr.colorrr.system.Typefaces

class ButtonSemiBold @JvmOverloads constructor(
    c: Context,
    attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatButton(c, attrs, defStyle) {
    init {
        /*if (attrs != null) {
            for (i in 0 until attrs.attributeCount) {
                if (attrs.getAttributeName(i) == "text" && !isInEditMode)
                    setText(attrs.getAttributeResourceValue(i, R.string.Error))
            }
        }*/
        this.setFont(c)
    }

    private fun setFont(context: Context) {
        val face = Typefaces[context, "fonts/GillSans_SemiBold.ttf"]
        setTypeface(face, Typeface.NORMAL)
        //typeface = face
    }

    fun setText(stringID: Int?) {
        val text = stringID?.let { this.context.getString(it) }
        super.setText(text, BufferType.NORMAL)
    }
}
