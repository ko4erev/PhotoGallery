package android.mobdev.com.photogallery

class GalleryItem {

     var mCaption: String? = null
     var mId: String? = null
     var mUrl: String? = null

    override fun toString(): String {
        return mCaption ?: ""
    }
}