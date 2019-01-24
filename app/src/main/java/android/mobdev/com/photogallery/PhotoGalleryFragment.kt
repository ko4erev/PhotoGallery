package android.mobdev.com.photogallery

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class PhotoGalleryFragment : Fragment() {
    private var mPhotoRecyclerView: RecyclerView? = null
    private var mItems = ArrayList<GalleryItem>()

    companion object {
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        FetchItemsTask().execute()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view) as RecyclerView
        mPhotoRecyclerView?.layoutManager = GridLayoutManager(activity, 3)
        setupAdapter()
        return v
    }

    private fun setupAdapter() {
        if (isAdded) {
            mPhotoRecyclerView?.adapter = PhotoAdapter(mItems)
        }
    }

    private inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mTitleTextView: TextView = itemView as TextView

        fun bindGalleryItem(item: GalleryItem) {
            mTitleTextView.text = item.toString()
        }
    }

    private inner class PhotoAdapter(private val mGalleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PhotoHolder {
            val textView = TextView(activity)
            return PhotoHolder(textView)
        }

        override fun onBindViewHolder(photoHolder: PhotoHolder, position: Int) {
            val galleryItem = mGalleryItems[position]
            photoHolder.bindGalleryItem(galleryItem)
        }

        override fun getItemCount(): Int {
            return mGalleryItems.size
        }
    }

    private inner class FetchItemsTask : AsyncTask<Void, Void, List<GalleryItem>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<GalleryItem> {
            return FlickrFetchr().fetchItems()
        }

        override fun onPostExecute(items: List<GalleryItem>) {
            mItems = items as ArrayList<GalleryItem>
            setupAdapter()
        }
    }
}