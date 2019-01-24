package android.mobdev.com.photogallery

import android.net.Uri
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class FlickrFetchr {

    private val TAG = "FlickrFetchr"
    private val API_KEY = "1bab082052d7cf8b3aa9e2bc92882ac0"

    fun getUrlBytes(urlSpec: String): ByteArray {
        val url = URL(urlSpec)
        val connection = url.openConnection() as HttpURLConnection
        try {
            var out = ByteArrayOutputStream()
            var iN = connection.inputStream
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException(connection.responseMessage + ": with " + urlSpec)
            }
            val buffer = ByteArray(1024)
            while (true) {
                val bytesRead = iN.read(buffer)
                if (bytesRead < 0) break
                out.write(buffer, 0, bytesRead)
            }
            out.close()
            return out.toByteArray()
        } finally {
            connection.disconnect()
        }
    }

    @Throws(IOException::class)
    fun getUrlString(urlSpec: String): String {
        return String(getUrlBytes(urlSpec))
    }


    fun fetchItems(): ArrayList<GalleryItem> {
        val items = ArrayList<GalleryItem>()
        try {
            val url = Uri.parse("https://api.flickr.com/services/rest/")
                .buildUpon()
                .appendQueryParameter("method", "flickr.photos.getRecent")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s")
                .build().toString()
            val jsonString = getUrlString(url)
            Log.i(TAG, "Received JSON: $jsonString")
            val jsonBody = JSONObject(jsonString)
            parseItems(items, jsonBody)
        } catch (ioe: IOException) {
            Log.e(TAG, "Failed to fetch items", ioe)
        } catch (je: JSONException) {
            Log.e(TAG, "Failed to parse JSON", je)
        }
        return items
    }

    @Throws(Exception::class)
    fun parseItems(items: ArrayList<GalleryItem>, jsonBody: JSONObject) {
        val photosJsonObject = jsonBody.getJSONObject("photos")
        val photoJsonArray = photosJsonObject.getJSONArray("photo")

        for (i in 0 until photoJsonArray.length()) {
            val photoJsonObject = photoJsonArray.getJSONObject(i)
            var item = GalleryItem()
            item.mId = photoJsonObject.getString("id")
            item.mCaption = photoJsonObject.getString("title")
            if (!photoJsonObject.has("url_s")) {
                continue
            }
            item.mUrl = (photoJsonObject.getString("url_s"))
            items.add(item)
        }
    }
}