package com.azimzada.healthapp.helpers.photoimport

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RoundedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val path = Path()
    private val rect = RectF()

    init {
        init()
    }

    private fun init() {

    }

    override fun onDraw(canvas: Canvas) {
        val radius = width / 2.toFloat()
        rect.set(0f, 0f, width.toFloat(), height.toFloat())

        path.reset()
        path.addCircle(radius, radius, radius, Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}