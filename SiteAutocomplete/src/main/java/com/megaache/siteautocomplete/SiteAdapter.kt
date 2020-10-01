/*
 * Copyright (C) 2020 Younes Megaache
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.megaache.siteautocomplete


import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ArrayAdapter
import android.widget.TextView
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.model.QuerySuggestionRequest
import com.huawei.hms.site.api.model.QuerySuggestionResponse
import com.huawei.hms.site.api.model.SearchStatus
import com.huawei.hms.site.api.model.Site
import java.util.*

/**
 * An adapter for the autocompleteTextView showing the result fo the Site kit queryAutocomplete
 */
class SiteAdapter(
    cx: Context,
    private val searchService: SearchService,
    private val resourceId: Int,
    items: ArrayList<Site>
) : ArrayAdapter<Site>(cx, resourceId, items) {

    companion object {
        const val TAG = "SAF-SiteAdapter"
        const val NO_ERROR = -1
        const val ITEM_ANIMATION_DELAY = 50 //in milliseconds
    }

    //callback to update the activity with the loading state
    var onLoadingStateChanged: ((isLoading: Boolean) -> (Unit))? = null

    //callback to update the activity when an error occur
    var onError: ((error: Int) -> (Unit))? = null

    //liveData containing the search result (suggestions)
    private val items: MutableList<Site>

    //holds the value of the last query, to ensure we always show the correct result
    private var query: String = ""

    /**
     * set the data return from HMS site service
     * @param newItems List<Site>? list of suggestion to show
     */
    fun setData(newItems: List<Site>?) {

        this.items.clear()
        if (newItems != null) {
            this.items.addAll(newItems)
            Log.d(TAG, "setData: " + newItems.size)
        }
        this.notifyDataSetChanged()
    }

    /**
     * Search for places using the HMS site kit,
     * called every time the user modifies the text inside the textView
     * @param query to pass to Site kit, got from SiteAutocompleteFragment.autoCompleteTextView
     */
    fun searchSites(query: String) {
        setData(null)
        this.query = query
        if (query.isEmpty()) {
            return
        }

        // Declare ang instantiate SearchService object.

        // Create a request body.
        val request = QuerySuggestionRequest()
        request.query = query

        // Create a searchSites result listener.
        val resultListener = object : SearchResultListener<QuerySuggestionResponse> {

            // Return searchSites results upon a successful searchSites.
            override fun onSearchResult(res: QuerySuggestionResponse) {

                onLoadingStateChanged?.invoke(false)

                if (request.query != this@SiteAdapter.query) {
                    //the query has changed by the time we got this result,
                    // we must ignore it
                    // we should clear the existing result if the new query is empty
                    setData(null)
                    return
                }

                val sites = res.sites
                if (sites == null || sites.size <= 0) {
                    return
                }
                Log.d(TAG, "searchSites result")
                for (site in sites) {
                    Log.d(TAG, site.name)
                }

                setData(sites)
            }

            // Return the result code and description upon a searchSites exception.
            override fun onSearchError(status: SearchStatus?) {
                onLoadingStateChanged?.invoke(false)

                if (status != null) {
                    onError?.invoke(status.errorCode.toInt())
                    Log.i(TAG, "Error : " + status.errorCode + " " + status.errorMessage)
                } else {
                    onError?.invoke(0)
                }
            }
        }

        onLoadingStateChanged?.invoke(true)
        onError?.invoke(NO_ERROR)

        // Call the searchSites suggestion API.
        searchService.querySuggestion(request, resultListener)
    }

    init {
        this.items = items
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        try {
            if (convertView == null) {
                val inflater = (context as Activity).layoutInflater
                view = inflater.inflate(resourceId, parent, false)
            }

            val site = getItem(position) ?: Site()

            val name = view!!.findViewById<TextView>(R.id.text1)
            val address = view.findViewById<TextView>(R.id.text2)

            //set the site fields to be shown
            name.text = site.name
            address.text = site.formatAddress ?: site.name


            //animate the view with a delay depending on its position in the list,
            //to get a staggered effect
            val animation: Animation? = loadAnimation(context, R.anim.fade_in_up)
            animation!!.duration = 200
            animation.startOffset = position * ITEM_ANIMATION_DELAY.toLong()
            view.startAnimation(animation)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return view!!
    }

    override fun getItem(position: Int): Site? {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


}
