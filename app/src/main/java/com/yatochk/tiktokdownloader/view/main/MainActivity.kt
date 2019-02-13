package com.yatochk.tiktokdownloader.view.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.yatochk.tiktokdownloader.R
import com.yatochk.tiktokdownloader.dagger.App
import com.yatochk.tiktokdownloader.utils.*
import com.yatochk.tiktokdownloader.view.RatingDialog
import com.yatochk.tiktokdownloader.view.download.DownloadFragment
import com.yatochk.tiktokdownloader.view.galery.GalleryFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainView {

    private val presenter = App.component.mainPresenter

    private val adapter = MainPagerAdapter(
        supportFragmentManager,
        listOf(
            DownloadFragment(),
            GalleryFragment()
        )
    )

    private lateinit var mInterstitialAd: InterstitialAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        MobileAds.initialize(this, getString(R.string.ad_mob_app_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.interstitial_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.tab_download)))
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.tab_history)))
        tab_layout.tabGravity = TabLayout.GRAVITY_FILL

        main_pager.adapter = adapter
        main_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                main_pager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withActionBarDrawerToggle(true)
            .withActionBarDrawerToggleAnimated(true)
            .withHeader(R.layout.drow_header)
            .addDrawerItems(
                PrimaryDrawerItem()
                    .withName(R.string.drawer_downloader)
                    .withBadge("ad")
                    .withOnDrawerItemClickListener { _, _, _ ->
                        presenter.clickInstaDownloader()
                        true
                    },
                PrimaryDrawerItem()
                    .withName(R.string.drawer_rate)
                    .withOnDrawerItemClickListener { _, _, _ ->
                        presenter.clickRate()
                        true
                    },
                PrimaryDrawerItem()
                    .withName(R.string.drawer_recommend)
                    .withOnDrawerItemClickListener { _, _, _ ->
                        presenter.clickRecommend()
                        true
                    },
                PrimaryDrawerItem()
                    .withName(R.string.drawer_feedback)
                    .withOnDrawerItemClickListener { _, _, _ ->
                        presenter.clickFeedback()
                        true
                    },
                PrimaryDrawerItem()
                    .withName(R.string.drawer_privacy)
                    .withOnDrawerItemClickListener { _, _, _ ->
                        presenter.clickPrivacy()
                        true
                    }
            )
            .build()

        checkPermission()
    }

    override var showDeleteToolbar: Boolean = false
        set(value) {
            button_delete_tool.visibility = if (value) View.VISIBLE else View.INVISIBLE
            field = value
        }

    override fun goToHistory() =
        main_pager.setCurrentItem(1, true)

    override fun showRate() {
        if (App.isDownloaded) {
            App.adCount++
            App.isDownloaded = false
        }

        val adDelayCount = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(LATER_COUNT_PREF, RATE_COUNT)
        val isNeverRate = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(NEVER_PREF, 0) == 1

        if (App.adCount > adDelayCount && !isNeverRate) {
            RatingDialog().show(supportFragmentManager, "rating")
            App.adCount = 0
        } else if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
    }

    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                USER_PERISSION_GRANTED
            )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            USER_PERISSION_GRANTED -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }

                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.bindView(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.unbindView()
    }

    override fun onBackPressed() {
        if (main_pager.currentItem == 1)
            main_pager.setCurrentItem(0, true)
        showRate()
    }

    override fun sendFeedback() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val height = metrics.heightPixels
        val width = metrics.widthPixels
        val manager = applicationContext.packageManager
        var info: PackageInfo? = null
        try {
            info = manager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val version = info!!.versionName
        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(getString(R.string.developer_email)))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + version)
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "\n" + " Device :" + getDeviceName() +
                    "\n" + " System Version:" + Build.VERSION.SDK_INT +
                    "\n" + " Display Height  :" + height + "px" +
                    "\n" + " Display Width  :" + width + "px" +
                    "\n\n" + getString(R.string.have_problem) +
                    "\n"
        )
        startActivity(Intent.createChooser(intent, "Send Email"))
    }

    override fun showPrivacy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url)))
        startActivity(intent)
    }
}

