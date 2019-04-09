package app.colorrr.colorrr.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.Utils

class RoundedRelativeLayout : RelativeLayout {
    private var cornerRadius: Float = 0.toFloat()

    constructor(context: Context): super(context){
        this.init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        this.init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        this.init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedRelativeLayout, defStyleAttr, 0)

        this.cornerRadius =
            attributes.getDimensionPixelSize(R.styleable.RoundedRelativeLayout_cornerRadius, 0).toFloat()
        this.cornerRadius = Utils.convertDpToPixelInt(context, this.cornerRadius).toFloat()

        attributes.recycle()
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val count = canvas.save()
        val path = Path()

        path.addRoundRect(
            RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()),
            this.cornerRadius,
            this.cornerRadius,
            Path.Direction.CW
        )

        canvas.clipPath(path, Region.Op.REPLACE)
        canvas.clipPath(path)

        super.dispatchDraw(canvas)

        canvas.restoreToCount(count)
    }
}
