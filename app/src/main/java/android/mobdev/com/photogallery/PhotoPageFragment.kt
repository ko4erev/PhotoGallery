package android.mobdev.com.photogallery

import android.net.Uri
import android.webkit.WebView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class PhotoPageFragment : VisibleFragment() {
    companion object {
        private val ARG_URI = "photo_page_url"

        fun newInstance(uri: Uri): PhotoPageFragment {
            val args = Bundle()
            args.putParcelable(ARG_URI, uri)
            val fragment = PhotoPageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mUri: Uri? = null
    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUri = arguments?.getParcelable(ARG_URI)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_photo_page, container, false)
        mWebView = v.findViewById(R.id.webView) as WebView
        return v
    }
}