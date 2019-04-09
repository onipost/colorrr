package app.colorrr.colorrr.system

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Point
import androidx.appcompat.app.AlertDialog
import org.jetbrains.anko.windowManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import app.colorrr.colorrr.App
import app.colorrr.colorrr.R
import org.joda.time.*
import java.io.ByteArrayOutputStream

object Utils {

    fun getWindowHeight(c: Context): Int {
        val display = c.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    fun showAlertDialogString(activity: Activity, title: Int, message: String, negativeButton: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.applicationContext.getString(title))
            .setMessage(message)
            .setCancelable(false)
            .setNegativeButton(
                activity.applicationContext.getString(negativeButton)
            ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    fun showAlertDialogInt(activity: Activity, title: Int, message: Int, negativeButton: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.applicationContext.getString(title))
            .setMessage(activity.getString(message))
            .setCancelable(false)
            .setNegativeButton(
                activity.applicationContext.getString(negativeButton)
            ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    fun showAlertDialogStringWithAction(
        activity: Activity,
        title: Int,
        message: String,
        negativeButton: Int,
        positiveButton: Int,
        dialogPositive: DialogInterface.OnClickListener
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.applicationContext.getString(title))
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(activity.applicationContext.getString(positiveButton), dialogPositive)
            .setNegativeButton(
                activity.applicationContext.getString(negativeButton)
            ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    fun showAlertDialogStringWithActions(
        activity: Activity,
        title: Int,
        message: String,
        negativeButton: Int,
        positiveButton: Int,
        dialogPositive: DialogInterface.OnClickListener,
        dialogNegative: DialogInterface.OnClickListener
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.applicationContext.getString(title))
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(activity.applicationContext.getString(positiveButton), dialogPositive)
            .setNegativeButton(activity.applicationContext.getString(negativeButton), dialogNegative)
        builder.create().show()
    }

    fun showAlertDialogIntWithAction(
        activity: Activity,
        title: Int,
        message: Int,
        negativeButton: Int,
        positiveButton: Int,
        dialogPositive: DialogInterface.OnClickListener
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.applicationContext.getString(title))
            .setMessage(activity.applicationContext.getString(message))
            .setCancelable(false)
            .setPositiveButton(activity.applicationContext.getString(positiveButton), dialogPositive)
            .setNegativeButton(
                activity.applicationContext.getString(negativeButton)
            ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun generateViewId(): Int {
        return View.generateViewId()
    }

    fun convertDpToPixelInt(context: Context, dp: Float): Int {
        val resources = context.resources
        val metrics = resources.displayMetrics
        val dpi = metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
        return (dpi * dp).toInt()
    }

    fun getTimeAgo(timestamp: Double): String {
        val app = App.getInstance()
        DateTimeZone.setDefault(DateTimeZone.UTC)
        val postDate = DateTime(timestamp.toLong() * 1000)
        val nowDate = DateTime()
        val period = Period(postDate, nowDate, PeriodType.yearMonthDayTime())
        return when {
            period.years == 1 -> app.getString(R.string._1_year_ago)
            period.years > 1 -> app.getString(R.string.i_years_ago).replace("%d", period.years.toString())
            period.months == 1 -> app.getString(R.string._1_month_ago)
            period.months > 1 -> app.getString(R.string.i_months_ago).replace("%d", period.months.toString())
            period.days == 1 -> app.getString(R.string._1_day_ago)
            period.days > 1 -> app.getString(R.string.i_days_ago).replace("%d", period.days.toString())
            period.hours == 1 -> app.getString(R.string._1_hour_ago)
            period.hours > 1 -> app.getString(R.string.i_hours_ago).replace("%d", period.hours.toString())
            period.minutes == 1 -> app.getString(R.string._1_minute_ago)
            period.minutes > 1 -> app.getString(R.string.i_minutes_ago).replace("%d", period.minutes.toString())
            period.seconds == 1 -> app.getString(R.string._1_second_ago)
            period.seconds > 1 -> app.getString(R.string.i_seconds_ago).replace("%d", period.seconds.toString())
            else -> ""
        }
    }

    fun getColorStateList(context: Context, resourceID: Int): ColorStateList {
        return if (Build.VERSION.SDK_INT > 22)
            context.resources.getColorStateList(resourceID, context.theme)
        else
            context.resources.getColorStateList(resourceID)
    }
}