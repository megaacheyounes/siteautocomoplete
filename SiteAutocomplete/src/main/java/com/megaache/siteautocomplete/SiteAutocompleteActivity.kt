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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.Site
import com.megaache.siteautocomplete.SiteAutocompleteAdapter.Companion.NO_ERROR
import kotlinx.android.synthetic.main.activity_support_map_fragment.*
import java.util.*
import kotlin.concurrent.thread


internal class SiteAutocompleteActivity : AppCompatActivity() {

    companion object {
        //static variable the will hold the site that the user chooses,
        //it will be accessed by the fragment after this activity finishes
        //because the Site class is not serializable and part of the Site kit
        var EXTRA_SITE: Site? = null
        const val EXTRA_FLAG_FULL_SCREEN = "EXTRA_FLAG_FULL_SCREEN"

        const val TEXT = "text"

        //tag, duh!
        private val TAG = SiteAutocompleteFragment.TAG

        //time after user stops typing to assume he finished typing
        //the search request will be sent after 500ms of the last letter entered if not more letter is entered
        const val DEBOUNCE_DURATION = 800

    }

    private var first = true
    private var isInFullScreenMode: Boolean = false

    //the autocompleteAdapter holding the list of the suggestions
    internal lateinit var autocompleteAdapter: SiteAutocompleteAdapter

    //the clear button (x on the left side of the edit text)
    private lateinit var clearMenuButton: MenuItem

    //hms api key
    private lateinit var hmsApiKey: String


    private var searchRunnable = Runnable { }

    /**
     * will run [searchRunnable] after delay [DEBOUNCE_DURATION], to multiple request for each latter the user enter in the editText
     */
    private var handler = Handler(Looper.myLooper()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        isInFullScreenMode = intent.getBooleanExtra(EXTRA_FLAG_FULL_SCREEN, false)
        if (isInFullScreenMode) {
            setTheme(R.style.theme_fullscreen)
        } else {
            setTheme(R.style.theme_overlay)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        super.onCreate(savedInstanceState)

        setContent()

        thread(start = true) {
            //get api from agconnect-services.json
            val keyTemp = AGConnectServicesConfig.fromContext(this).getString("client/api_key")
            hmsApiKey = Uri.encode(keyTemp)
            runOnUiThread {
                //initialize ui
                initView()
            }
        }

        //request an immediate focus for the text view, to bring up the keyboard and save the user a tap!
        editText.requestFocus()
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun setContent() {
        setContentView(R.layout.activity_support_map_fragment)

        //setup the toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 4f

        if (!isInFullScreenMode) {
            //on overlay mode the toolbar is always white,
            //we have to change to color of the home button (arrow) to gray, and so for the clear button
            window.setGravity(Gravity.TOP)
        }
    }

    private fun initView() {
        val searchService: SearchService = SearchServiceFactory.create(this, hmsApiKey)

        //initiate the autocompleteAdapter
        autocompleteAdapter = SiteAutocompleteAdapter(
            this,
            searchService,
            R.layout.simple_list_item_2,
            ArrayList()
        )

        //observe the loading liveData, and update the UI whenever there is a change
        autocompleteAdapter.onLoadingStateChanged = this::loading
        loading(false)

        //observe the error liveData, and update the UI whenever there is a change
        autocompleteAdapter.onError = this::onError
        onError(NO_ERROR)
        //register a listener to try the last search when the "try again" link is clicked
        network_error_try_again.setOnClickListener {
            debounceSearch(editText.text.toString(), true)
        }
        //hide the list's default item divider
        list.divider = null
        //set the autocompleteAdapter
        list.adapter = autocompleteAdapter
        //register a listener to be invoked when an item is clicked
        list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val site = autocompleteAdapter.getItem(position)
            Log.d(TAG, "item clicked $position ${site!!.name}")
            sendResult(site)

        }
        //add a text watcher (change listener) to the edit text
        // to update the suggestion whenever there is a change
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (first) return
                debounceSearch(s.toString())
                updateClearButton()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

    }

    fun debounceSearch(query: String, immediate: Boolean = false) {
        //cancel all previous search requestes
        handler.removeCallbacksAndMessages(null)


        searchRunnable = Runnable {
            autocompleteAdapter.searchSites(query)
        }

        /**
         * send request after [DEBOUNCE_DURATION] ms, this will be canceled if user enter another letter into the search editText within [DEBOUNCE_DURATION]
         */
        handler.postDelayed(searchRunnable, DEBOUNCE_DURATION.toLong())

    }

    /**
     * clear the edit text if not empty
     * called when the clear button (X on the left of the edit Text) is clicked
     */
    private fun clear() {
        if (editText.text.isNotEmpty()) {
            editText.text.clear()
            updateClearButton()
        }
    }

    /**
     *
     * show the selected site's name in the search editText,
     * when enabled using [SiteAutocompleteFragment.showSelectedSiteOnSearchView]
     */
    private fun setSelectedSite() {
        intent.getStringExtra(TEXT)?.let {
            editText.setText(it)
            //move cursor to end of editText
            editText.setSelection(it.length)

            updateClearButton()
            first = false
        }
    }

    /**
     * show the clear button (x on the left side of the editText)
     * only when the edit text is not empty
     */
    fun updateClearButton() {
        clearMenuButton.isVisible = editText.text.isNotEmpty()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        clearMenuButton = menu!!.findItem(R.id.clear)
        //   if (!isInFullScreenMode) {

        val tintedIcon = DrawableCompat.wrap(clearMenuButton.icon!!)
        DrawableCompat.setTint(tintedIcon, ContextCompat.getColor(this, R.color.textPrimary))
        clearMenuButton.icon = tintedIcon
        // }
        clearMenuButton.isVisible = false

        setSelectedSite()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> clear()
            android.R.id.home -> {
                sendResult(null)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * handle the loading state, show the loading views when the service initiated a request and still didn't
     * return a result or an error
     * @param loading Boolean true if the site request didn't finish, false otherwise
     */
    fun loading(loading: Boolean) {
        if (loading) {
            loading_text.visibility = View.VISIBLE
            loading_spinner.visibility = View.VISIBLE
        } else {
            loading_text.visibility = View.INVISIBLE
            loading_spinner.visibility = View.INVISIBLE
        }
    }

    /**
     * handle the error state, show the error views and the code when the HMS site service return an error
     * after initiating a Site search request
     * @param error Int the error status code got from the last search
     */
    private fun onError(error: Int) {
        if (error == NO_ERROR) {
            network_error.visibility = View.GONE
            network_error_image.visibility = View.GONE
            network_error_try_again.visibility = View.GONE
        } else {
            network_error.visibility = View.VISIBLE
            network_error.text = resources.getString(R.string.network_error, error)
            network_error_image.visibility = View.VISIBLE
            network_error_try_again.visibility = View.VISIBLE
        }
    }


    /**
     * set the result, called when the user clicks on an item from the list of suggestion
     * then hide the keyboard and finish this activity
     * @param site Site?
     */
    private fun sendResult(site: Site?) {
        val i = Intent()
        EXTRA_SITE = site
//        if (site != null) {
//            i.putExtra(EXTRA_SITE, site)
//        }
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        //hide the keyboard
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
//        autocompleteAdapter.onError = null
//        autocompleteAdapter.onLoadingStateChanged = null
    }
}
