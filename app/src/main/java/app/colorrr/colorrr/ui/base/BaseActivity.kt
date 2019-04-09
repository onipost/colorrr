package app.colorrr.colorrr.ui.base

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import app.colorrr.colorrr.system.AnimationWorker
import kotlinx.android.synthetic.main._loader.*

abstract class BaseActivity<T : BaseViewModel<*>> : AppCompatActivity() {
    private var mViewModel: T? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(getLayoutID())

        this.mViewModel = if (mViewModel == null) this.getViewModel() else mViewModel

        loader?.setOnTouchListener { _, _ -> true }
    }

    override fun onDestroy() {
        this.mViewModel = null
        super.onDestroy()
    }

    abstract fun getLayoutID(): Int

    abstract fun getViewModel(): T

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showLoader(stringID: Int) {
        if (loader != null) {
            AnimationWorker.animateOverflowFade(loader, false, 300)
            AnimationWorker.animateRotateInfinite(loader_image, 5000 * 20L)
            loader_text.text = applicationContext.getString(stringID)
        }
    }

    fun hideLoader() {
        if (loader != null){
            AnimationWorker.animateOverflowFade(loader, true, 300)
        }
    }
}