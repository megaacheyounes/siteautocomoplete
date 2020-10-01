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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment

class SiteAutocompleteFragment : Fragment() {

    companion object {
        const val SITE_REQ_CODE = 12924
        val TAG: String = SiteAutocompleteFragment::class.java.simpleName
    }

    //display mode
    private var mode: SiteAutocompleteMode = SiteAutocompleteMode.FULLSCREEN

    //reference to the autocompleteTextView
    private lateinit var autoCompleteTextView: AppCompatAutoCompleteTextView

    /**
     * the callback that wil lbe invoked when user clicks on an item from the suggestions list
     */
    private lateinit var onSiteSelected: SiteSelectionListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v =
            inflater.inflate(R.layout.autocomplete_support_fragment, container, false) as ViewGroup

        autoCompleteTextView = v.findViewById(R.id.editText)

        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) startAutocompleteActivity()
        }

//        //for debugging
//        Handler(Looper.myLooper()!!).postDelayed({
//            autoCompleteTextView.requestFocus()
//        }, 500)

        return v
    }

    /**
     * set the display mode
     * @param mode SiteAutocompleteMode can be either [SiteAutocompleteMode.FULLSCREEN]
     * or [SiteAutocompleteMode.OVERLAY]
     */
    fun setActivityMode(mode: SiteAutocompleteMode) {
        this.mode = mode
    }

    /**
     * start the [SiteAutocompleteActivity] in the specified mode whenever the [autoCompleteTextView] is in focus
     */
    private fun startAutocompleteActivity() {
        val i = Intent(context, SiteAutocompleteActivity::class.java)

        i.putExtra(
            SiteAutocompleteActivity.EXTRA_FLAG_FULL_SCREEN,
            this.mode == SiteAutocompleteMode.FULLSCREEN
        )

        startActivityForResult(
            i,
            SITE_REQ_CODE
        )
    }


    /**
     * Called only when the [mode] equal [SiteAutocompleteMode.FULLSCREEN]
     * Hide the keyboard and invoke the callback sending the site the user tapped on
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        autoCompleteTextView.clearFocus()
        if (requestCode == SITE_REQ_CODE) {

            val temp = SiteAutocompleteActivity.EXTRA_SITE
            Log.d(TAG, "received result ${temp?.name}")

            temp?.let {
                onSiteSelected.onSiteSelected(temp)
            }

        }
    }

    /**
     * set the callback that will be invoked when the user clicks on an item of the suggestion list
     * Then initialize the view by starting the [SiteAutocompleteActivity]
     * @param onSiteSelected SiteSelectionListener the callback
     */
    fun setOnSiteSelectListener(onSiteSelected: SiteSelectionListener) {
        this.onSiteSelected = onSiteSelected
    }


}
