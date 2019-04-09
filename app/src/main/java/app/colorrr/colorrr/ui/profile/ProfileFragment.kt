package app.colorrr.colorrr.ui.profile

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.ProfileImagesListAdapter
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.system.GlideLoader
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseFragment
import app.colorrr.colorrr.ui.profile_edit.ProfileEditFragment
import app.colorrr.colorrr.ui.profile_followers.ProfileFollowersFragment
import app.colorrr.colorrr.views.ProfileFilterView
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment<ProfileViewModel>(), ProfileInterface {
    private lateinit var viewModel: ProfileViewModel
    private var adapter: ProfileImagesListAdapter? = null
    private var fromStringId: Int? = null

    /*
    * LIFECYCLE
    * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initVM(savedInstanceState)
        this.initGrid()
        this.initFilter()
        this.bindListeners()
    }

    override fun onResume() {
        super.onResume()
        back_text.text = this.viewModel.getString(this.fromStringId)
    }

    override fun onDestroy() {
        this.viewModel.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this.viewModel.isAnother())
            outState.putInt("id", this.viewModel.getProfileID())

        this.fromStringId?.let { outState.putInt("from", it) }
    }

    /*
    * BASE FRAGMENT METHODS
    * */
    override fun getLayoutID(): Int {
        return R.layout.fragment_profile
    }

    override fun getViewModel(): ProfileViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = ProfileViewModel()

        return this.viewModel
    }

    /*
    * VM-FRAGMENT INTERFACE METHODS
    * */
    override fun onProfileArrived(user: UserCurrent?, currentUserID: Int) {
        this.setData(user)
        this.adapter?.addData(this.viewModel.getImagesListByFilter())
    }

    /*
    * METHODS AND LISTENERS OF THIS FRAGMENT
    * */
    private fun initVM(savedInstanceState: Bundle?) {
        this.viewModel.setListener(this)
        this.viewModel.setProfileID(arguments?.getInt("id"))
        this.fromStringId = arguments?.getInt("from")
        savedInstanceState?.let {
            this.viewModel.setProfileID(it.getInt("id"))
            this.fromStringId = arguments?.getInt("from")
        }
    }

    private fun initFilter() {
        filter.setFilter(this.viewModel.getFilter())
    }

    private fun initGrid() {
        this.adapter = ProfileImagesListAdapter(getActivityCustom().applicationContext, ArrayList(), object : ProfileImagesListAdapter.ProfileImageLoadMoreListener {
            override fun onNeedMore() {
                //viewModel.loadMore()
            }
        })
        grid.layoutManager = GridLayoutManager(getActivityCustom(), 2)
        grid.adapter = this.adapter
    }

    private fun bindListeners() {
        edit.setOnClickListener { getNavigationController().pushFragment(ProfileEditFragment()) }

        back.setOnClickListener {
            this.viewModel.removeCached()
            getNavigationController().popFragment()
        }
        followers_count.setOnClickListener { this.openFollowers(0) }
        followed_count.setOnClickListener { this.openFollowers(1) }

        filter.setFilterItemClick(object : ProfileFilterView.FilterItemClick {
            override fun onClick(id: Int) {
                viewModel.filter(id, swipe_refresh)
            }
        })
    }

    private fun openFollowers(type: Int) {
        if (this.viewModel.getInternetConnection()) {
            val bundle = Bundle()
            bundle.putInt("id", this.viewModel.getProfileID())
            bundle.putInt("type", type)
            val fragment = ProfileFollowersFragment()
            fragment.arguments = bundle
            getNavigationController().pushFragment(fragment)
        } else {
            val message = if (type == 0)
                R.string.unable_to_load_your_followers_list_no_internet_connection_available_please_connect_to_internet_and_t
            else
                R.string.unable_to_load_list_of_users_youre_follow_no_internet_connection_available_please_connect_to_interne
            Utils.showAlertDialogInt(getActivityCustom(), R.string.Error, message, R.string.Ok)
        }
    }

    private fun setData(user: UserCurrent?) {
        //TODO follow action, follow icon
        user?.let {
            if (this.viewModel.isAnother()) {
                follow.visibility = View.VISIBLE
                back.visibility = View.VISIBLE
                filter.visibility = View.GONE
                edit.visibility = View.GONE
            } else {
                follow.visibility = View.GONE
                filter.visibility = View.VISIBLE
                edit.visibility = View.VISIBLE
                if (this.viewModel.isRoot())
                    back.visibility = View.GONE
            }

            published_count.text = it.publishedCount.toString()
            followers_count.text = it.followersCount.toString()
            followed_count.text = it.followedCount.toString()
            likes_count.text = it.likesCount.toString()

            if (it.image != "")
                GlideLoader.load(getActivityCustom().applicationContext, it.image, user_avatar)
            else
                user_avatar.setImageResource(R.drawable.mask)
        }
    }
}