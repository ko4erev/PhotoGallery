package android.mobdev.com.photogallery

import android.app.Activity
import android.content.IntentFilter
import android.support.v4.app.Fragment
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log


open class VisibleFragment : Fragment() {
    private val TAG = "VisibleFragment"

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollService.ACTION_SHOW_NOTIFICATION)
        activity?.registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null)
    }

    override fun onStop() {
        super.onStop()
        activity!!.unregisterReceiver(mOnShowNotification)
    }

    private val mOnShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Получение означает, что пользователь видит приложение,
            // поэтому оповещение отменяется
            Log.i(TAG, "canceling notification");
            resultCode = Activity.RESULT_CANCELED;
        }
    }
}