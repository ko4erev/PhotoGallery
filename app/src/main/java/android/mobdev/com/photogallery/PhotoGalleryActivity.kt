package android.mobdev.com.photogallery

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment

class PhotoGalleryActivity : SingleFragmentActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PhotoGalleryActivity::class.java)
        }
    }

    override fun createFragment(): Fragment {
        return PhotoGalleryFragment.newInstance()
    }

}
