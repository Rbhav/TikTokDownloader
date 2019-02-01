package com.yatochk.tiktokdownloader.model

import android.widget.ImageView
import java.io.File

interface Model {
    fun openAppInMarket(packageName: String)
    fun getCopyUrl(): String?
    fun downloadPreview(url: String, view: ImageView, listener: ((code: Int) -> Unit)?)
    fun downloadVideo(url: String, listener: ((String) -> Unit)?)
    fun shareApp()
    fun rate()
    fun getVideoFiles(): ArrayList<File>
    fun deleteVideo(path: String, listener: ((Boolean) -> Unit)?)
    fun shareVideo(path: String)
}