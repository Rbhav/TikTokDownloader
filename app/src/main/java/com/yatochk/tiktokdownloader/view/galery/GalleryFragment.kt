package com.yatochk.tiktokdownloader.view.galery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.yatochk.tiktokdownloader.R
import com.yatochk.tiktokdownloader.dagger.App
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.File

class GalleryFragment : Fragment(), Gallery {
    private val adapter = GalleryRecyclerAdapter()
    private val presenter = App.component.galleryPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.bindView(this)
        recycler_video.layoutManager = GridLayoutManager(activity, 4)
        recycler_video.adapter = adapter
    }

    override fun updateVideos(files: ArrayList<File>) {
        adapter.submitList(files)
    }
}