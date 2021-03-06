<a href="http://developer.android.com/index.html" target="_blank"><img src="https://img.shields.io/badge/platform-android-green.svg"/></a>
<a href="https://android-arsenal.com/api?level=21"><img src="https://img.shields.io/badge/API-21%2B-green.svg?style=flat" border="0" alt="API"></a>
<a href="https://opensource.org/licenses/Apache-2.0" target="_blank"><img src="https://img.shields.io/badge/License-Apache_v2.0-blue.svg?style=flat"/></a>
[![uses gms](https://img.shields.io/badge/USES-GMS-green.svg)](https://shields.io/)
[![uses hms](https://img.shields.io/badge/USES-HMS-red.svg)](https://shields.io/)

# An AutocompleteSupportFragment for HMS Site kit

This library provides a fragment similar to the Google Places [AutocompleteSupportFragment](https://developers.google.com/places/android-sdk/autocomplete), for the HMS [Site kit](https://developer.huawei.com/consumer/en/hms/huawei-sitekit)
Shows suggestions when searching for places

## Configuration

**WARNING**: Before you can start using the HMS Platform APIs and SDKs, you must register your app in the AppGallery and configure your project, this step cannot be skipped.

See [configuration Instructions](CONFIGURATION.md).

## Installation
add jitpack repo to your project level `build.gradle` file:

``` groovy
        allprojects {
    		repositories {
    			maven { url 'https://jitpack.io' }
    		}
    	}
```
add the library to your app level 'build.bradle' file:
``` groovy
	dependencies {
	        implementation 'com.github.megaacheyounes:siteautocomoplete:1.0.5'
	}
```

## Add the fragment
see MainActivity and MainActivityJava for complete example

### Method 1 (xml):
add the fragment tag with the name attribute:
``` xml
    <fragment
        android:id="@+id/fragment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:name="com.megaache.siteautocomplete.SiteAutocompleteFragment">

    </fragment>
```
Initialize the fragment:
``` kotlin
    val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.fragment) as SiteAutocompleteFragment
```

### Method 2 (kotlin):
declare the fragment container:
``` xml
   <FrameLayout
        android:id="@+id/fragment_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

    </FrameLayout>
```
then add `SiteAutocompleteFragment` fragment dynamically using the `supportFragmentManager`:
``` kotlin
   val autocompleteFragment = SiteAutocompleteFragment()

   supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commit()

   supportFragmentManager.executePendingTransactions()
```

## Display mode


- Fullscreen mode:
<img alt="fullscreen mode" src="screenshots/fullscreen_mode.gif"  width="295" height="640" />

```kotlin
            autocompleteFragment.setActivityMode(SiteAutocompleteMode.FULLSCREEN)
```

- Overlay mode:
<img alt="overlay mode" src="screenshots/overlay_mode.gif"  width="295" height="640" />

```kotlin
            autocompleteFragment.setActivityMode(SiteAutocompleteMode.OVERLAY)
```

## Listen for Place selection event

```kotlin
          autocompleteFragment.setOnSiteSelectListener(
                object : SiteSelectionListener {
                    override fun onSiteSelected(site: Site) {
                        Log.d("onSiteSelected", site.name)
                    }
                    override fun onError(error: Error) {
                        Log.d("onError", error.toString())
                    }
                }
            )
```

## show selected site on search view:
if you call this method, when the user select a site (place), the name and the address of that place will replace the placeholder "search sites..." in search view (autocomplete EditText ),
if false passed then only the name of the selected site will be shown
```kotlin
         autocompleteFragment.showSelectedSiteOnSearchView(true)
```

## Internationalization
The fragment supports 8 languages: English, Arabic, Chinese, French, Hindi, German, Russian, Spanish

## Important

To run the example app you must create an app in huawei console, then add the config file 'agconnect-config.json' into the project and update the package name according to package name in config file


## Contributions

All kinds of contributions are welcome

## HMS
learn more about HMS: [**here**](https://developer.Huawei.com/consumer/en/hms)


```
  Copyright (C) 2020 Younes Megaache

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```









