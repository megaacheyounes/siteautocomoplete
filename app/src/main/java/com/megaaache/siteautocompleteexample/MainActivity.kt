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
package com.megaaache.siteautocompleteexample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.site.api.model.Site
import com.megaache.siteautocomplete.SiteAutocompleteFragment
import com.megaache.siteautocomplete.SiteAutocompleteMode
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val PLACES = "switch to google places"
        const val SITE = "switch to huawei site"
        const val TAG = "SAF-MainActivity"
    }

    private var isHms = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceFragment()
    }

    private fun replaceFragment() {
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as SiteAutocompleteFragment

        autocompleteFragment.run {
            setActivityMode(SiteAutocompleteMode.FULLSCREEN)
            setOnSiteSelectListener(
                object : SiteAutocompleteFragment.SiteSelectionListener {
                    override fun onSiteSelected(site: Site) {
                        Log.d("onSiteSelected", site.name)
                        toast("on site selected:  ${site.name}")
                        site.run {
                            setResult(
                                name,
                                formatAddress,
                                location.lat,
                                location.lng
                            )
                            showHuaweiSitePhoto(this)
                        }
                    }
                }
            )
        }
    }

    fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    fun setResult(
        title: String,
        address: String,
        latitude: Double,
        longitude: Double
    ) {
        name.text = title
        name2.text = address
        name3.text = latitude.toString()
        name4.text = longitude.toString()
    }


    /**
     * fetch and show a photo of the user's selected site, called when the hms Autocomplete fragment returns a result
     * @param site the site that the user selected
     */
    fun showHuaweiSitePhoto(site: Site) {
        if (site.poi == null) {
            return
        }
        site.poi.photoUrls?.let {
            Log.d(TAG, "selected site photo: ${site.poi.photoUrls[0]}")
            Picasso.get().load(site.poi.photoUrls[0]).into(photo)
        }
    }

}
