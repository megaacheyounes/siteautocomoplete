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
package com.megaaache.siteautocompleteexample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.site.api.model.Site;
import com.megaache.siteautocomplete.SiteAutocompleteFragment;
import com.megaache.siteautocomplete.SiteAutocompleteMode;
import com.megaache.siteautocomplete.SiteSelectionListener;

import org.jetbrains.annotations.NotNull;


class MainActivityJava extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment();
    }

    private void replaceFragment() {
        SiteAutocompleteFragment autocompleteFragment = (SiteAutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        assert autocompleteFragment != null;

        autocompleteFragment.setActivityMode(SiteAutocompleteMode.FULLSCREEN);

        autocompleteFragment.showSelectedSiteOnSearchView(true);

        autocompleteFragment.setOnSiteSelectListener(new SiteSelectionListener() {
            @Override
            public void onSiteSelected(@NotNull Site site) {
                Log.d("onSiteSelected", site.getName());
                toast("on site selected: " + site.getName());

                setResult(site.getName(),
                        site.getFormatAddress(),
                        site.getLocation().getLat(),
                        site.getLocation().getLng());
            }

            @Override
            public void onError(@NotNull Error error) {

            }
        });
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void setResult(
            String title,
            String address,
            Double latitude,
            Double longitude
    ) {
        ((TextView) findViewById(R.id.name)).setText(title);
        ((TextView) findViewById(R.id.name2)).setText(address);
        ((TextView) findViewById(R.id.name3)).setText(latitude.toString());
        ((TextView) findViewById(R.id.name4)).setText(longitude.toString());
    }


}
