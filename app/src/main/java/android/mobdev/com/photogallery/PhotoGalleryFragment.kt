package android.mobdev.com.photogallery

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.view.*
import android.support.v7.widget.SearchView


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
        setHasOptionsMenu(true)
        updateItems()

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

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu)
        val searchItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String?): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $s")
                    QueryPreferences.setStoredQuery(activity as Context, s)
                    updateItems()
                    return true
                }

                override fun onQueryTextChange(s: String?): Boolean {
                    Log.d(TAG, "QueryTextChange: $s")
                    return false
                }
            }
        )

        searchView.setOnSearchClickListener {
            val query = QueryPreferences.getStoredQuery(activity as Context)
            searchView.setQuery(query, false)
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        if (PollService.isServiceAlarmOn(activity as Context)) {
            toggleItem.setTitle(R.string.stop_polling)
        } else {
            toggleItem.setTitle(R.string.start_polling)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_clear -> {
            QueryPreferences.setStoredQuery(activity as Context, null)
            updateItems()
            true
        }
        R.id.menu_item_toggle_polling -> {
            val shouldStartAlarm = !PollService.isServiceAlarmOn(activity as Context)
            PollService.setServiceAlarm(activity as Context, shouldStartAlarm)
            activity?.invalidateOptionsMenu()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun updateItems() {
        val query = QueryPreferences.getStoredQuery(activity as Context)
        query?.let { FetchItemsTask(it).execute() }
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

    private inner class FetchItemsTask() : AsyncTask<Void, Void, List<GalleryItem>>() {

        private var mQuery: String? = null

        constructor(query: String) : this() {
            mQuery = query
        }

        override fun doInBackground(vararg params: Void?): List<GalleryItem> {
            return if (mQuery == null) {
                FlickrFetchr().fetchRecentPhotos()
            } else {
                FlickrFetchr().searchPhotos(mQuery)
            }
        }

        override fun onPostExecute(items: List<GalleryItem>) {
            mItems = items as ArrayList<GalleryItem>
            setupAdapter()
        }
    }
}