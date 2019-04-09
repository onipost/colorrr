package app.colorrr.colorrr.ui.login_create

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseActivity
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main._toolbar.*
import kotlinx.android.synthetic.main.activity_login_create.*
import android.graphics.Bitmap
import app.colorrr.colorrr.ui.main.MainActivity
import pl.aprilapps.easyphotopicker.EasyImage
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class LoginCreateActivity : BaseActivity<LoginCreateViewModel>(), LoginCreateInterface,
    EasyPermissions.PermissionCallbacks {
    private lateinit var viewModel: LoginCreateViewModel
    private var disposable: Disposable? = null
    private var photoDisposable: Disposable? = null

    /*
     * Lifecycle
      * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewModel.setListener(this)
        this.initToolbar()
        this.subscribe()
        this.viewModel.init()
        EasyImage.configuration(this).setImagesFolderName("Colorrr")
        photo.setOnClickListener { this.viewModel.selectPhoto(this) }
    }

    override fun onDestroy() {
        disposable?.dispose()
        photoDisposable?.dispose()
        EasyImage.clearConfiguration(this)
        super.onDestroy()
    }

    /*
     * Permissions request
      * */

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /*
     * Catch data from camera/gallery
      * */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.viewModel.handleOnActivityResult(this, requestCode, resultCode, data)
    }

    /*
     * Menu create callbacks (in toolbar)
      * */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.confirm_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.confirm) {
            this.hideKeyboard()
            this.showLoader(R.string.creating_account)
            this.viewModel.createUser(
                name.text.toString(),
                email.text.toString(),
                re_email.text.toString(),
                password.text.toString(),
                re_password.text.toString()
            )
        }

        return true
    }

    /*
    * BaseActivity methods override
    * */

    override fun getLayoutID(): Int {
        return R.layout.activity_login_create
    }

    override fun getViewModel(): LoginCreateViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = LoginCreateViewModel()

        return this.viewModel
    }

    /*
     * LoginCreate interface override
      * */

    override fun onCreateUser(result: Boolean, message: String) {
        this.hideLoader()
        if (result)
            startActivity(Intent(applicationContext, MainActivity::class.java))
        else
            Utils.showAlertDialogString(this, R.string.Error, message, R.string.Ok)
    }

    /*
     * Permission callbacks
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
     * Activity methods
      * */

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar_text.text = applicationContext.getString(R.string.create_account)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun subscribe() {
        this.photoDisposable = this.viewModel.getPhoto().subscribe { value: Bitmap ->
            photo_preview.setImageBitmap(value)
        }
    }
}