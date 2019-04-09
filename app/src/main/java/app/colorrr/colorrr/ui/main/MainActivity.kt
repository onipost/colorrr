package app.colorrr.colorrr.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import app.colorrr.colorrr.NavigationController
import app.colorrr.colorrr.R
import app.colorrr.colorrr.ui.base.BaseActivity
import app.colorrr.colorrr.ui.categories.CategoriesFragment
import app.colorrr.colorrr.ui.feed.FeedFragment
import app.colorrr.colorrr.ui.profile.ProfileFragment
import app.colorrr.colorrr.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<MainViewModel>(), MainInterface {
    private var pages: ArrayList<View> = ArrayList()
    private var tabsPosition = 0
    private lateinit var categoriesNavigation: NavigationController
    private lateinit var feedNavigation: NavigationController
    private lateinit var profileNavigation: NavigationController
    private lateinit var settingsNavigation: NavigationController

    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.viewModel.setListener(this)
        this.initTabs()
        this.initViewPager()

        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_categories -> view_pager.setSelectedItem(0)
                R.id.nav_feed -> view_pager.setSelectedItem(1)
                R.id.nav_profile -> view_pager.setSelectedItem(2)
                R.id.nav_settings -> view_pager.setSelectedItem(3)
                else -> {
                    view_pager.setSelectedItem(0)
                }
            }
            true
        }
    }


    override fun onBackPressed() {
        if (this.pages[this.tabsPosition] is NavigationController) {
            val activeController = this.pages[this.tabsPosition] as NavigationController

            if (activeController.getFragmentsCount() > 1) {
                activeController.getFragment(activeController.getFragmentsCount() - 1).onBackPressed()
                //activeController.popFragment();
                return
            }
        }

        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }

    override fun getLayoutID(): Int {
        return R.layout.activity_main
    }

    override fun getViewModel(): MainViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = MainViewModel()

        return this.viewModel
    }

    private fun initTabs() {
        this.categoriesNavigation = NavigationController(this)
        this.categoriesNavigation.pushFragment(CategoriesFragment())
        this.pages.add(this.categoriesNavigation)

        this.feedNavigation = NavigationController(this)
        this.feedNavigation.pushFragment(FeedFragment())
        this.pages.add(this.feedNavigation)

        this.profileNavigation = NavigationController(this)
        this.profileNavigation.pushFragment(ProfileFragment())
        this.pages.add(this.profileNavigation)

        this.settingsNavigation = NavigationController(this)
        this.settingsNavigation.pushFragment(SettingsFragment())
        this.pages.add(this.settingsNavigation)
    }

    private fun initViewPager() {
        view_pager.adapter = MainAdapter(this.pages)
        view_pager.setSelectedItem(0)

        val pageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                tabsPosition = position
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        }

        if (Build.VERSION.SDK_INT < 24)
            view_pager.setOnPageChangeListener(pageChangeListener)
        else
            view_pager.addOnPageChangeListener(pageChangeListener)
    }
}