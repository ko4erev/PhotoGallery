package android.mobdev.com.photogallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment

class PhotoPageActivity : SingleFragmentActivity() {

    companion object {
        fun newIntent(context: Context, photoPageUri: Uri): Intent {
            val i = Intent(context, PhotoPageActivity::class.java)
            i.data = photoPageUri
            return i
        }
    }

    override fun createFragment(): Fragment {
        return PhotoPageFragment.newInstance(intent.data)
    }
}