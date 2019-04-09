package app.colorrr.colorrr.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.colorrr.colorrr.NavigationController
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.AnimationWorker

abstract class BaseFragment<T : BaseViewModel<*>> : Fragment() {
    private var mViewModel: T? = null
    private var mActivity: BaseActivity<*>? = null
    private lateinit var navigationController: NavigationController

    private var mLoader: LinearLayout? = null
    private var mLoaderImage: ImageView? = null
    private var mLoaderText: TextView? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is BaseActivity<*>)
            this.mActivity = context
    }

    override fun onDetach() {
        this.mActivity = null
        this.mViewModel = null
        super.onDetach()
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mViewModel = if (mViewModel == null) this.getViewModel() else mViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(getLayoutID(), null)
        this.mLoader = activity?.findViewById(R.id.loader)
        this.mLoaderImage = activity?.findViewById(R.id.loader_image)
        this.mLoaderText = activity?.findViewById(R.id.loader_text)

        this.mLoader?.let { it.setOnTouchListener { _, _ -> true } }

        return view
    }

    abstract fun getLayoutID(): Int

    abstract fun getViewModel(): T

    fun hideKeyboard() {
        mActivity?.hideLoader()
    }

    open fun showLoader(stringID: Int) {
        this.mLoader?.let { AnimationWorker.animateOverflowFade(it, false, 300) }
        this.mLoaderImage?.let { AnimationWorker.animateRotateInfinite(it, 5000 * 20L) }
        this.mLoaderText?.let { it.text = context?.getString(stringID) ?: "" }
    }

    open fun hideLoader() {
        this.mLoader?.let { AnimationWorker.animateOverflowFade(it, true, 300) }
    }

    fun setNavigationController(value: NavigationController) {
        this.navigationController = value
    }

    fun getActivityCustom(): AppCompatActivity {
        return navigationController.getActivity()
    }

    fun getNavigationController(): NavigationController {
        return navigationController
    }

    fun onBackPressed() {
        navigationController.popFragment()
    }

    fun onBackPressed(bundle: Bundle) {
        navigationController.popFragment(bundle)
    }
}