package app.colorrr.colorrr.ui.profile_followers

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.FollowersListAdapter
import app.colorrr.colorrr.entity.Follower
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseFragment
import app.colorrr.colorrr.ui.profile.ProfileFragment
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main._toolbar.*
import kotlinx.android.synthetic.main.fragment_profile_followers.*

class ProfileFollowersFragment : BaseFragment<ProfileFollowersViewModel>(), ProfileFollowersInterface,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var viewModel: ProfileFollowersViewModel
    private var adapter: FollowersListAdapter? = null

    /*
    * LIFECYCLE
    * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initVM(savedInstanceState)
        this.initToolbar()
        this.initViews()
        this.bindListeners()
    }

    override fun onResume() {
        super.onResume()
        this.showEmpty()
        //TODO change string
        showLoader(R.string.loading_user_profile)
        this.viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        this.viewModel.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this.viewModel.isAnother())
            outState.putInt("id", this.viewModel.getProfileID())
    }

    /*
    * BASE FRAGMENT METHODS
    * */
    override fun getLayoutID(): Int {
        return R.layout.fragment_profile_followers
    }

    override fun getViewModel(): ProfileFollowersViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = ProfileFollowersViewModel()

        return this.viewModel
    }

    /*
    * VM-FRAGMENT INTERFACE METHODS
    * */
    override fun onFollowersArrived(refreshList: Boolean, data: List<Follower>) {
        hideLoader()
        this.hideEmpty()
        if (refreshList) {
            swipe_refresh.isRefreshing = false
            this.adapter?.clearData()
        }

        this.adapter?.addData(data)
    }

    override fun onError(error: String) {
        hideLoader()
        swipe_refresh.isRefreshing = false
        Utils.showAlertDialogString(getActivityCustom(), R.string.Error, error, R.string.Ok)
    }

    /*
    * UI LISTENERS
    * */
    override fun onRefresh() {
        this.viewModel.refresh()
    }

    /*
    * METHODS AND LISTENERS OF THIS FRAGMENT
    * */
    private fun initVM(savedInstanceState: Bundle?) {
        this.viewModel.setListener(this)
        this.viewModel.setProfileID(arguments?.getInt("id"))
        savedInstanceState?.let { this.viewModel.setProfileID(it.getInt("id")) }
    }

    private fun initToolbar() {
        this.viewModel.setType(arguments?.getInt("type"))
        toolbar_text.text = this.viewModel.getToolbarText()
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initViews() {
        list.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        this.adapter = FollowersListAdapter(
            getActivityCustom().applicationContext,
            ArrayList(),
            this.viewModel.getLoginUserID(),
            this.openProfileClickListener,
            this.followClickListener,
            this.loadMoreListener
        )
        list.adapter = this.adapter

        empty_text.text = this.viewModel.getEmptyText()
    }

    private fun bindListeners() {
        swipe_refresh.setOnRefreshListener(this)
    }

    private fun showEmpty() {
        swipe_refresh.visibility = View.GONE
        empty.visibility = View.VISIBLE
    }

    private fun hideEmpty() {
        empty.visibility = View.GONE
        swipe_refresh.visibility = View.VISIBLE

    }

    private val openProfileClickListener = object : FollowersListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Follower) {
            showLoader(R.string.loading_user_profile)
            var mDisposable: Disposable? = null
            mDisposable = viewModel.loadUserProfile(item.userID)
                .doAfterNext { mDisposable?.dispose() }
                .subscribeBy(
                    onNext = {
                        hideLoader()
                        val fragment = ProfileFragment()
                        val bundle = Bundle()
                        bundle.putInt("from", viewModel.getBackProfileStringId())
                        if (it.userID != viewModel.getLoginUserID()) {
                            bundle.putInt("id", it.userID)
                        }
                        fragment.arguments = bundle
                        getNavigationController().pushFragment(fragment)
                    },
                    onError = {
                        hideLoader()
                        Utils.showAlertDialogString(
                            getActivityCustom(),
                            R.string.Error,
                            it.localizedMessage,
                            R.string.Ok
                        )
                    }
                )
        }
    }

    private val followClickListener = object : FollowersListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Follower) {
            if (item.isFollowed == 0) {
                viewModel.follow(item)
            } else {
                val message = viewModel.getString(R.string.are_you_sure_you_want_to_unfollow_user)
                    .replace("%s", item.userName)
                Utils.showAlertDialogStringWithAction(
                    getActivityCustom(),
                    R.string.unfollow_user,
                    message,
                    R.string.Cancel,
                    R.string.Unfollow,
                    DialogInterface.OnClickListener { dialog, _ ->
                        viewModel.follow(item)
                        dialog.cancel()
                    })
            }
        }
    }

    private val loadMoreListener = object : FollowersListAdapter.LoadMoreListener {
        override fun onLoad() {
            viewModel.loadData()
        }
    }
}