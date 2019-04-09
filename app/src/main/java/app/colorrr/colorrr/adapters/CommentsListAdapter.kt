package app.colorrr.colorrr.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Comment
import app.colorrr.colorrr.system.GlideLoader
import app.colorrr.colorrr.system.Utils
import org.jetbrains.anko.layoutInflater

class CommentsListAdapter(private var c: Context, private var data: ArrayList<Comment>) :
    ArrayAdapter<Comment>(c, R.layout.item_comment) {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Comment? {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return data[position].id?.toLong() ?: 0L
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = data[position]
        val mView: View = convertView ?: c.layoutInflater.inflate(R.layout.item_comment, parent, false)

        val mUserAvatar = mView.findViewById<ImageView>(R.id.user_avatar)
        val mUserName = mView.findViewById<TextView>(R.id.user_name)
        val mCommentText = mView.findViewById<TextView>(R.id.text)
        val mCommentTime = mView.findViewById<TextView>(R.id.comment_time)
        val mDelimiter = mView.findViewById<LinearLayout>(R.id.delimiter)

        if (item.senderAvatar != "")
            GlideLoader.load(context, item.senderAvatar, mUserAvatar)

        mUserName.text = item.senderName
        mCommentText.text = item.text
        mCommentTime.text = Utils.getTimeAgo(item.timestamp)
        mDelimiter.visibility = if (position == data.size - 1) View.GONE else View.VISIBLE

        return mView
    }
}