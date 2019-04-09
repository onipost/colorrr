package app.colorrr.colorrr

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.FrameLayout
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseFragment
import java.util.ArrayList

class NavigationController : FrameLayout {
    private var activity: AppCompatActivity
    private var fragmentManager: FragmentManager
    private var pendingFragment: Fragment? = null
    private var fragmentsStack = ArrayList<BaseFragment<*>>()

    constructor(context: Context) : super(context) {
        this.activity = context as AppCompatActivity
        this.fragmentManager = this.activity.supportFragmentManager
        id = Utils.generateViewId()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context) {
        this.activity = context as AppCompatActivity
        this.fragmentManager = this.activity.supportFragmentManager
        id = Utils.generateViewId()
    }

    fun pushFragment(fragment: BaseFragment<*>) {
        fragment.setNavigationController(this)
        fragmentsStack.add(fragment)

        parent?.let {
            replaceFragment(fragment)
        } ?: run {
            pendingFragment = fragment
        }
    }

    fun popFragment() {
        if (fragmentsStack.size <= 1)
            return

        fragmentsStack.removeAt(fragmentsStack.size - 1)
        replaceFragment(fragmentsStack[fragmentsStack.size - 1])
    }

    fun popFragment(bundle: Bundle) {
        if (fragmentsStack.size <= 1)
            return

        fragmentsStack.removeAt(fragmentsStack.size - 1)
        replaceFragment(fragmentsStack[fragmentsStack.size - 1], bundle)
    }

    fun getFragmentsCount(): Int {
        return fragmentsStack.size
    }

    fun getFragment(i: Int): BaseFragment<*> {
        return fragmentsStack[i]
    }

    fun getActivity(): AppCompatActivity {
        return this.activity
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        pendingFragment?.let {
            replaceFragment(it)
            pendingFragment = null
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (fragmentsStack.size == 0) {
            fragmentTransaction.add(id, fragment)
        } else {
            fragmentTransaction.replace(id, fragment)
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        }

        fragmentTransaction.commit()
    }

    private fun replaceFragment(fragment: Fragment, bundle: Bundle?) {
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (fragmentsStack.size == 0) {
            if (bundle != null) {
                fragment.arguments = bundle
            }
            fragmentTransaction.add(id, fragment)
        } else {
            if (bundle != null) {
                fragment.arguments = bundle
            }
            fragmentTransaction.replace(id, fragment)
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        }

        fragmentTransaction.commit()
    }
}
