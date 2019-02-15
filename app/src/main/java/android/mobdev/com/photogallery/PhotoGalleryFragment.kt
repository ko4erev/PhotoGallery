package android.mobdev.com.photogallery

import android.content.ContentValues.TAG
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap


class PhotoGalleryFragment : Fragment() {
    private var mPhotoRecyclerView: RecyclerView? = null
    private var mItems = ArrayList<GalleryItem>()
    private var mThumbnailDownloader: ThumbnailDownloader<PhotoHolder>? = null

    companion object {
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        FetchItemsTask().execute()

        val responseHandler = Handler()
        mThumbnailDownloader = ThumbnailDownloader(responseHandler)
        mThumbnailDownloader?.setThumbnailDownloadListener(
            object : ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder> {
                override fun onThumbnailDownloaded(photoHolder: PhotoHolder, bitmap: Bitmap) {
                    val drawable = BitmapDrawable(resources, bitmap)
                    photoHolder.bindDrawable(drawable)
                }
            }
        )
        mThumbnailDownloader?.start()
        mThumbnailDownloader?.looper
        Log.i(TAG, "Background thread started")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view) as RecyclerView
        mPhotoRecyclerView?.layoutManager = GridLayoutManager(activity, 3)
        setupAdapter()
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mThumbnailDownloader?.clearQueue()
    }

    override fun onDestroy() {
        super.onDestroy()
        mThumbnailDownloader?.quit()
        Log.i(TAG, "Background thread destroyed")
    }

    private fun setupAdapter() {
        if (isAdded) {
            mPhotoRecyclerView?.adapter = PhotoAdapter(mItems)
        }
    }

    inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mItemImageView: ImageView = itemView.findViewById(R.id.item_image_view) as ImageView

        fun bindDrawable(drawable: Drawable) {
            mItemImageView.setImageDrawable(drawable)
        }
    }

    private inner class PhotoAdapter(private val mGalleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PhotoHolder {
            val inflater = LayoutInflater.from(activity)
            val view = inflater.inflate(R.layout.gallery_item, viewGroup, false)
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(photoHolder: PhotoHolder, position: Int) {
            val galleryItem = mGalleryItems[position]
            galleryItem.mUrl?.let { mThumbnailDownloader?.queueThumbnail(photoHolder, it) }
        }

        override fun getItemCount(): Int {
            return mGalleryItems.size
        }
    }

    private inner class FetchItemsTask : AsyncTask<Void, Void, List<GalleryItem>>() {

        override fun doInBackground(vararg params: Void?): List<GalleryItem> {
            val query = "robot" // Для тестирования
            return if (query == null) {
                FlickrFetchr().fetchRecentPhotos()
            } else {
                FlickrFetchr().searchPhotos(query)
            }
        }

        override fun onPostExecute(items: List<GalleryItem>) {
            mItems = items as ArrayList<GalleryItem>
            setupAdapter()
        }
    }
}