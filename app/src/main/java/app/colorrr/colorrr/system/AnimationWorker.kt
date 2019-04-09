package app.colorrr.colorrr.system

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat

object AnimationWorker {
    const val LOAD_PLACEHOLDER: Long = 100000


    fun animateOverflowFade(view: View, reverse: Boolean, duration: Long) {
        val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
        animator.duration = duration

        if (!reverse) {
            view.visibility = View.VISIBLE
            ViewCompat.setTranslationZ(view, 100F)
            animator.start()
        } else {
            animator.doOnEnd {
                ViewCompat.setTranslationZ(view, 0F)
                view.visibility = View.GONE
            }
            animator.reverse()
        }
    }

    fun animateHeight(view: View, from: Int, to: Int, duration: Long) {
        val slideAnimator = ValueAnimator.ofInt(from, to).setDuration(duration)
        slideAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }

        val set = AnimatorSet()
        set.play(slideAnimator)
        set.interpolator = LinearInterpolator()
        set.start()
    }

    fun animateRotateInfinite(view: View, duration: Long): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f * 20)
        animator.duration = duration
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = Animation.INFINITE
        animator.start()
        return animator
    }
}