package app.colorrr.colorrr.ui.base

import java.lang.ref.WeakReference

abstract class BaseViewModel<L> {
    private var listener: WeakReference<L>? = null

    fun setListener(listener: L?) {
        this.listener = listener?.let { WeakReference(listener) } ?: run { null }
    }

    fun getListener(): L? {
        return listener?.get()
    }
}