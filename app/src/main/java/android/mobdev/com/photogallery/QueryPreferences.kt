package android.mobdev.com.photogallery

import android.content.Context
import android.preference.PreferenceManager


class QueryPreferences {
    companion object {
        private val PREF_SEARCH_QUERY = "searchQuery"
        private val PREF_LAST_RESULT_ID = "lastResultId"

        fun getStoredQuery(context: Context): String? {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null)
        }

        fun setStoredQuery(context: Context, query: String?) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply()
        }

        fun getLastResultId(context: Context): String? {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null)
        }

        fun setLastResultId(contex: Context, lastResultId: String?) {
            PreferenceManager.getDefaultSharedPreferences(contex)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                .apply()
        }
    }
}