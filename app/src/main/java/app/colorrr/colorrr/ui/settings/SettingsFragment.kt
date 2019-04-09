package app.colorrr.colorrr.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.colorrr.colorrr.R
import app.colorrr.colorrr.ui.base.BaseFragment

class SettingsFragment : BaseFragment<SettingsViewModel>(), SettingsInterface {
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.viewModel.setListener(this)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getLayoutID(): Int {
        return R.layout.fragment_settings
    }

    override fun getViewModel(): SettingsViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = SettingsViewModel()

        return this.viewModel
    }
}