package app.colorrr.colorrr.ui.feed

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.FeedListAdapter
import app.colorrr.colorrr.entity.Feed
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseFragment
import app.colorrr.colorrr.ui.feed_item.FeedItemFragment
import app.colorrr.colorrr.ui.profile.ProfileFragment
import app.colorrr.colorrr.views.FeedFilterView
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_feed.*

class FeedFragment : BaseFragment<FeedViewModel>(),
    FeedInterface,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var viewModel: FeedViewModel
    private var adapter: FeedListAdapter? = null

    /*
    * LIFECYCLE
    * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getActivityCustom().getString(R.string.Discover)
        this.initViews()
        this.bindListeners()
    }

    override fun onResume() {
        super.onResume()
        list.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        this.viewModel.setListener(this)
        this.viewModel.getData()
    }

    override fun onPause() {
        super.onPause()
        this.viewModel.setListener(null)
    }

    /*
    * BASE FRAGMENT METHODS
    * */
    override fun getLayoutID(): Int {
        return R.layout.fragment_feed
    }

    override fun getViewModel(): FeedViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = FeedViewModel()

        return this.viewModel
    }

    /*
    * VM-FRAGMENT INTERFACE METHODS
    * */
    override fun onDataArrived(refreshList: Boolean, data: List<Feed>) {
        if (refreshList)
            this.adapter?.clearData()

        this.adapter?.addData(data)
    }

    override fun onFollowAndLike(success: Boolean, error: String) {
        if (!success)
            Utils.showAlertDialogString(getActivityCustom(), R.string.Error, error, R.string.Ok)
        else
            this.adapter?.notifyDataSetChanged()
    }

    override fun onReport(success: Boolean, error: String) {
        hideLoader()
        var title: Int = R.string.Error
        var errorString = error
        if (success) {
            title = R.string.item_reported
            errorString = viewModel
                .getString(R.string.thank_you_for_reporting_item_well_review_and_remove_it_from_colorrr_comminity_if_required)
        }

        Utils.showAlertDialogString(getActivityCustom(), title, errorString, R.string.Ok)
    }

    override fun onDelete(position: Int, success: Boolean, error: String) {
        if (success) {
            this.adapter?.removeItem(position)
        } else {
            Utils.showAlertDialogString(getActivityCustom(), R.string.Error, error, R.string.Ok)
        }
    }

    /*
    * UI LISTENERS
    * */
    override fun onRefresh() {
        this.viewModel.refresh(swipe_refresh)
    }

    /*
    * METHODS AND LISTENERS OF THIS FRAGMENT
    * */
    private fun initViews() {
        filter.setFilter(this.viewModel.getFilter())
        this.adapter = FeedListAdapter(
            getActivityCustom(),
            ArrayList(),
            this.viewModel.getLoginUserID(),
            this.openCommentsClickListener,
            this.followClickListener,
            this.likeClickListener,
            this.reportClickListener,
            this.continueClickListener,
            this.deleteClickListener,
            this.openProfileClickListener,
            this.loadMoreListener
        )
        list.adapter = this.adapter
    }

    private fun bindListeners() {
        filter.setFilterItemClick(object : FeedFilterView.FilterItemClick {
            override fun onClick(id: Int) {
                viewModel.filter(id, swipe_refresh)
            }
        })

        swipe_refresh.setOnRefreshListener(this)
    }

    private val likeClickListener = object : FeedListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Feed) {
            viewModel.like(item)
        }
    }

    private val followClickListener = object : FeedListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Feed) {
            if (item.followed == 0) {
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

    private val reportClickListener = object : FeedListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Feed) {
            val message = viewModel.getString(R.string.are_you_sure_you_want_to_report_this_item_by)
                .replace("%s", item.userName)
            Utils.showAlertDialogStringWithAction(
                getActivityCustom(),
                R.string.report_item,
                message,
                R.string.Cancel,
                R.string.Report,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                    showLoader(R.string.reporting_item)
                    viewModel.reportItem(item)
                })
        }
    }

    private val deleteClickListener = object : FeedListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Feed) {
            Utils.showAlertDialogIntWithAction(
                getActivityCustom(),
                R.string.delete_item,
                R.string.are_you_sure_you_want_to_remove_this_item_from_colorrr_community,
                R.string.Cancel,
                R.string.Delete,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                    showLoader(R.string.deleting_item)
                    viewModel.deleteItem(position, item)
                })
        }
    }

    private val loadMoreListener = object : FeedListAdapter.LoadMoreListener {
        override fun onLoad() {
            viewModel.loadMore()
        }
    }

    private val continueClickListener = object : FeedListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Feed) {
            //TODO handle
        }
    }

    private val openProfileClickListener = object : FeedListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Feed) {
            if (item.userID != viewModel.getUserID()) {
                if (viewModel.getInternetConnection()) {
                    showLoader(R.string.loading_user_profile)
                    var mDisposable: Disposable? = null
                    mDisposable = viewModel
                        .loadUserProfile(item.userID)
                        .subscribeBy(
                            onNext = {
                                mDisposable?.dispose()
                                hideLoader()
                                val fragment = ProfileFragment()
                                val bundle = Bundle()
                                bundle.putBoolean("another", true)
                                bundle.putInt("from", R.string.Discover)
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
                } else {
                    Utils.showAlertDialogInt(
                        getActivityCustom(),
                        R.string.Error,
                        R.string.unable_to_load_users_profile_no_internet_connection_available_please_connect_to_internet_and_try_aga,
                        R.string.Ok
                    )
                }
            } else {
                getNavigationController().pushFragment(ProfileFragment())
            }
        }
    }

    private val openCommentsClickListener = object : FeedListAdapter.ItemClickListener {
        override fun onClick(position: Int, item: Feed) {
            if (viewModel.getInternetConnection()) {
                val fragment = FeedItemFragment()
                val bundle = Bundle()
                bundle.putInt("id", item.id ?: 0)
                fragment.arguments = bundle
                getNavigationController().pushFragment(fragment)
            } else {
                Utils.showAlertDialogInt(
                    getActivityCustom(),
                    R.string.Error,
                    R.string.unable_to_load_comments_for_post_no_internet_connection_available_please_connect_to_internet_and_try,
                    R.string.Ok
                )
            }
        }
    }
}