package app.colorrr.colorrr.ui.feed_item

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.CommentsListAdapter
import app.colorrr.colorrr.entity.Comment
import app.colorrr.colorrr.entity.Feed
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.GlideLoader
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseFragment
import app.colorrr.colorrr.ui.profile.ProfileFragment
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main._toolbar.*
import kotlinx.android.synthetic.main.fragment_feed_item.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk27.coroutines.onScrollListener

class FeedItemFragment : BaseFragment<FeedItemViewModel>(),
    FeedItemInterface,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var viewModel: FeedItemViewModel
    private var adapter: CommentsListAdapter? = null
    private var mHeader: View? = null
    private val comments = ArrayList<Comment>()

    /*
    * LIFECYCLE
    * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.showLoader(R.string.loading_comments)
        this.initVM(savedInstanceState)
        this.initGrid()
        this.bindListeners()
    }

    override fun onDestroy() {
        this.viewModel.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("id", this.viewModel.getPostID())
    }

    /*
    * BASE FRAGMENT METHODS
    * */
    override fun getLayoutID(): Int {
        return R.layout.fragment_feed_item
    }

    override fun getViewModel(): FeedItemViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = FeedItemViewModel()

        return this.viewModel
    }

    /*
    * VM-FRAGMENT INTERFACE METHODS
    * */
    override fun onPostArrived(post: Feed) {
        getActivityCustom()?.let { this.setPost(it.applicationContext, post) }
    }

    override fun onCommentsArrived(refreshList: Boolean, list: List<Comment>) {
        this.hideLoader()
        if (refreshList) {
            this.comments.clear()
            this.adapter?.clear()
        }

        this.comments.addAll(list)
        this.adapter?.notifyDataSetChanged()
    }

    override fun onLike(success: Boolean, error: String) {
        if (!success) {
            this.setLike()
            getActivityCustom()?.let { Utils.showAlertDialogString(it, R.string.Error, error, R.string.Ok) }
        }
    }

    override fun onFollow(success: Boolean, error: String) {
        if (!success) {
            this.setFollow()
            getActivityCustom()?.let { Utils.showAlertDialogString(it, R.string.Error, error, R.string.Ok) }
        }
    }

    override fun onReport(success: Boolean, message: String) {
        this.hideLoader()
        getActivityCustom()?.let {
            if (!success)
                Utils.showAlertDialogString(it, R.string.Error, message, R.string.Ok)
            else
                Utils.showAlertDialogString(it, R.string.item_reported, message, R.string.Ok)
        }
    }

    override fun onDelete(success: Boolean, message: String) {
        this.hideLoader()
        getActivityCustom()?.let {
            if (!success)
                Utils.showAlertDialogString(it, R.string.Error, message, R.string.Ok)
            else
                getNavigationController()?.popFragment()
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
    private fun initVM(savedInstanceState: Bundle?) {
        this.viewModel.setListener(this)
        this.viewModel.setPostID(arguments?.getInt("id") ?: 0)
        savedInstanceState?.let { this.viewModel.setPostID(it.getInt("id")) }
    }

    private fun bindListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        swipe_refresh.setOnRefreshListener(this)
    }

    private fun initGrid() {
        getActivityCustom()?.let {
            toolbar_text.text = it.getString(R.string.Comments)
            this.mHeader = context?.layoutInflater?.inflate(R.layout.item_feed, null)
            grid.addHeaderView(this.mHeader, false)
            grid.numColumns = 1
            this.adapter = CommentsListAdapter(it.applicationContext, this.comments)
            grid.adapter = this.adapter
        }

        grid.onScrollListener {
            onScroll { _, firstVisibleItem, visibleItemCount, totalItemCount ->
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    viewModel.loadMore()
                }
            }
        }
    }

    private fun setPost(context: Context, item: Feed) {
        val mPostTime = this.mHeader?.findViewById<TextView>(R.id.post_time)
        val mPostLikesCount = this.mHeader?.findViewById<TextView>(R.id.post_likes_count)
        val mPostCommentsCount = this.mHeader?.findViewById<TextView>(R.id.post_comments_count)
        val mUserName = this.mHeader?.findViewById<TextView>(R.id.user_name)
        val mPlaceholder = this.mHeader?.findViewById<ImageView>(R.id.placeholder)
        val mPostLiked = this.mHeader?.findViewById<ImageView>(R.id.post_liked)
        val mFollow = this.mHeader?.findViewById<ImageView>(R.id.follow)
        val mUserAvatar = this.mHeader?.findViewById<ImageView>(R.id.user_avatar)
        val mPostPreview = this.mHeader?.findViewById<ImageView>(R.id.post_preview)
        val mPostReColor = this.mHeader?.findViewById<ImageView>(R.id.post_recolor)
        val mInfo = this.mHeader?.findViewById<ImageView>(R.id.info)

        if (mPlaceholder != null && mPostPreview != null) {
            val animator = AnimationWorker.animateRotateInfinite(mPlaceholder, AnimationWorker.LOAD_PLACEHOLDER)
            GlideLoader.load(context, item.linkPreview, mPostPreview, mPlaceholder, animator)
        }

        mPostTime?.text = Utils.getTimeAgo(item.timestamp)
        mPostCommentsCount?.text = item.commentsCount.toString()

        mPostLikesCount?.text = item.likesCount.toString()
        mPostLiked?.setImageResource(if (item.liked == 1) R.drawable.like else R.drawable.button_like)
        mFollow?.setImageResource(if (item.followed == 1) R.drawable.follow_ok else R.drawable.follow)
        mFollow?.tag = item.userID
        mUserName?.text = item.userName
        if (item.userAvatar != "" && mUserAvatar != null)
            GlideLoader.load(context, item.userAvatar, mUserAvatar)

        mUserAvatar?.setOnClickListener { this.openProfileListener.onClick(item) }
        mFollow?.setOnClickListener { this.followClickListener.onClick(item) }
        mPostLiked?.setOnClickListener { this.likeClickListener.onClick(item) }
        mInfo?.setOnClickListener { view ->
            getActivityCustom()?.let { this.infoClickListener.onOpen(it, view, item, this.viewModel.getUserID()) }
        }

        mPostReColor?.setOnClickListener {
            //TODO handle
        }
    }

    private fun reverseLike() {
        val item = this.viewModel.getPost()
        val mPostLikesCount = this.mHeader?.findViewById<TextView>(R.id.post_likes_count)
        val mPostLiked = this.mHeader?.findViewById<ImageView>(R.id.post_liked)
        item?.let {
            if (it.liked == 1) {
                mPostLikesCount?.text = (it.likesCount - 1).toString()
                mPostLiked?.setImageResource(R.drawable.button_like)
            } else {
                mPostLikesCount?.text = (it.likesCount + 1).toString()
                mPostLiked?.setImageResource(R.drawable.like)
            }
        }
    }

    private fun setLike() {
        val item = this.viewModel.getPost()
        val mPostLikesCount = this.mHeader?.findViewById<TextView>(R.id.post_likes_count)
        val mPostLiked = this.mHeader?.findViewById<ImageView>(R.id.post_liked)
        item?.let {
            mPostLikesCount?.text = it.likesCount.toString()
            mPostLiked?.setImageResource(if (item.liked == 1) R.drawable.like else R.drawable.button_like)
        }
    }

    private fun reverseFollow() {
        val item = this.viewModel.getPost()
        val mFollow = this.mHeader?.findViewById<ImageView>(R.id.follow)
        item?.let { mFollow?.setImageResource(if (item.followed == 1) R.drawable.follow else R.drawable.follow_ok) }
    }

    private fun setFollow() {
        val item = this.viewModel.getPost()
        val mFollow = this.mHeader?.findViewById<ImageView>(R.id.follow)
        item?.let { mFollow?.setImageResource(if (item.followed == 1) R.drawable.follow_ok else R.drawable.follow) }
    }

    private val followClickListener = object : PostActionListener {
        override fun onClick(item: Feed) {
            reverseFollow()
            if (item.followed == 0) {
                viewModel.follow()
            } else {
                val message = viewModel.getString(R.string.are_you_sure_you_want_to_unfollow_user)
                    .replace("%s", item.userName)
                getActivityCustom()?.let {
                    Utils.showAlertDialogStringWithActions(
                        it,
                        R.string.unfollow_user,
                        message,
                        R.string.Cancel,
                        R.string.Unfollow,
                        DialogInterface.OnClickListener { dialog, _ ->
                            viewModel.follow()
                            dialog.cancel()
                        },
                        DialogInterface.OnClickListener { dialog, _ ->
                            setFollow()
                            dialog.cancel()
                        })
                }
            }
        }
    }

    private val likeClickListener = object : PostActionListener {
        override fun onClick(item: Feed) {
            reverseLike()
            viewModel.setLike()
        }
    }

    private val reportClickListener = object : PostActionListener {
        override fun onClick(item: Feed) {
            getActivityCustom()?.let { activity ->
                if (viewModel.getInternetConnection()) {
                    val message = viewModel.getString(R.string.are_you_sure_you_want_to_report_this_item_by)
                        .replace("%s", item.userName)
                    Utils.showAlertDialogStringWithAction(
                        activity,
                        R.string.report_item,
                        message,
                        R.string.Cancel,
                        R.string.Report,
                        DialogInterface.OnClickListener { dialog, _ ->
                            dialog.cancel()
                            showLoader(R.string.reporting_item)
                            viewModel.sendReport()
                        })
                } else {
                    onReport(false, viewModel.getString(R.string.no_internet_connection))
                }
            }

        }
    }

    private val deleteClickListener = object : PostActionListener {
        override fun onClick(item: Feed) {
            getActivityCustom()?.let { activity ->
                if (viewModel.getInternetConnection()) {
                    Utils.showAlertDialogIntWithAction(
                        activity,
                        R.string.delete_item,
                        R.string.are_you_sure_you_want_to_remove_this_item_from_colorrr_community,
                        R.string.Cancel,
                        R.string.Delete,
                        DialogInterface.OnClickListener { dialog, _ ->
                            dialog.cancel()
                            showLoader(R.string.deleting_item)
                            viewModel.delete()
                        })
                } else {
                    onDelete(false, viewModel.getString(R.string.no_internet_connection))
                }
            }
        }
    }

    private val continueClickListener = object : PostActionListener {
        override fun onClick(item: Feed) {
            //TODO handle
        }
    }

    private val openProfileListener = object : PostActionListener {
        override fun onClick(item: Feed) {
            if (item.userID != viewModel.getUserID()) {
                getActivityCustom()?.let { activity ->
                    if (viewModel.getInternetConnection()) {
                        showLoader(R.string.loading_user_profile)
                        val loadUserDisposable = viewModel
                            .loadUserProfile(item.userID)
                            .subscribeBy(
                                onNext = {
                                    hideLoader()
                                    val fragment = ProfileFragment()
                                    val bundle = Bundle()
                                    bundle.putInt("id", it.userID)
                                    bundle.putInt("from", R.string.Comments)
                                    fragment.arguments = bundle
                                    getNavigationController()?.pushFragment(fragment)
                                },
                                onError = {
                                    hideLoader()
                                    Utils.showAlertDialogString(
                                        activity,
                                        R.string.Error,
                                        it.localizedMessage,
                                        R.string.Ok
                                    )
                                }
                            )
                    } else {
                        Utils.showAlertDialogInt(
                            activity,
                            R.string.Error,
                            R.string.unable_to_load_users_profile_no_internet_connection_available_please_connect_to_internet_and_try_aga,
                            R.string.Ok
                        )
                    }
                }
            } else {
                getNavigationController()?.pushFragment(ProfileFragment())
            }
        }
    }

    private val infoClickListener = object : PostOpenInfoListener {
        override fun onOpen(activity: Activity, view: View, item: Feed, userID: Int) {
            val popup = PopupMenu(activity, view)
            popup.inflate(if (userID != item.userID) R.menu.feed_item_menu else R.menu.feed_item_user_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.report -> {
                        reportClickListener.onClick(item)
                        true
                    }
                    R.id.continue_draw -> {
                        continueClickListener.onClick(item)
                        true
                    }
                    R.id.delete -> {
                        deleteClickListener.onClick(item)
                        true
                    }
                    else -> true
                }
            }
            popup.show()
        }
    }

    interface PostActionListener {
        fun onClick(item: Feed)
    }

    interface PostOpenInfoListener {
        fun onOpen(activity: Activity, view: View, item: Feed, userID: Int)
    }
}