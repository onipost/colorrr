package app.colorrr.colorrr.ui.profile_edit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.GlideLoader
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseFragment
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main._toolbar.*
import kotlinx.android.synthetic.main.fragment_profile_edit.*
import pl.aprilapps.easyphotopicker.EasyImage
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class ProfileEditFragment : BaseFragment<ProfileEditViewModel>(), ProfileEditInterface,
    EasyPermissions.PermissionCallbacks {
    private lateinit var viewModel: ProfileEditViewModel
    private var mProfileDisposable: Disposable? = null
    private var mProfileImageDisposable: Disposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initToolbar()
        this.viewModel.setListener(this)
        this.viewModel.init()
        this.bindListeners()
    }

    override fun onResume() {
        super.onResume()
        this.bindToData()
    }

    override fun onPause() {
        super.onPause()
        this.mProfileDisposable?.dispose()
        this.mProfileImageDisposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.viewModel.setListener(null)
    }

    /*
     * PERMISSIONS REQUEST
      * */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.viewModel.handleOnActivityResult(this, getActivityCustom(), requestCode, resultCode, data)
    }

    /*
     * PERMISSIONS CALLBACKS
      * */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        EasyImage.openChooserWithGallery(this, this.getString(R.string.select_a_photo), this.viewModel.IMAGE_PICKER)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    /*
     * MENU
      * */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.confirm_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.confirm) {
            this.hideKeyboard()
            this.showLoader(R.string.updating_account)
            this.viewModel.updateUser(
                name.text.toString(),
                email.text.toString(),
                re_email.text.toString(),
                password.text.toString(),
                new_password.text.toString()
            )
        }
        return true
    }

    /*
    * BASE FRAGMENT METHODS
    * */
    override fun getLayoutID(): Int {
        return R.layout.fragment_profile_edit
    }

    override fun getViewModel(): ProfileEditViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = ProfileEditViewModel()

        return this.viewModel
    }

    /*
    * VM-FRAGMENT INTERFACE METHODS
    * */
    override fun onUserUpdated() {
        hideLoader()
        getNavigationController().popFragment()
    }

    override fun onUpdateUserError(error: String) {
        hideLoader()
        Utils.showAlertDialogString(getActivityCustom(), R.string.Error, error, R.string.Ok)
    }

    /*
    * METHODS AND LISTENERS OF THIS FRAGMENT
    * */
    private fun initToolbar() {
        getActivityCustom().setSupportActionBar(toolbar)
        toolbar_text.text = getActivityCustom().applicationContext.getString(R.string.edit_profile)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun bindListeners() {
        photo.setOnClickListener { this.viewModel.selectPhoto(this) }
    }

    private fun bindToData() {
        this.mProfileDisposable = this.viewModel.user.subscribeBy {
            GlideLoader.load(getActivityCustom(), it.image, photo_preview)
            name.setText(it.name)
            email.setText(it.email)
            re_email.setText(it.email)
        }

        this.mProfileImageDisposable = this.viewModel.userPhoto.subscribeBy {
            Log.d("PROFILE_EDIT", it.toString())
            photo_preview.setImageBitmap(it)
        }
    }
}