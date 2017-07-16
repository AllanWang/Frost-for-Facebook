package com.pitchedapps.frost.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import ca.allanwang.kau.utils.bindView
import com.github.chrisbanes.photoview.PhotoView
import com.pitchedapps.frost.R
import com.sothree.slidinguppanel.SlidingUpPanelLayout

/**
 * Created by Allan Wang on 2017-07-15.
 */
class ImageActivity : AppCompatActivity() {

    val panel: SlidingUpPanelLayout by bindView(R.id.image_panel)
    val photo: PhotoView by bindView(R.id.image_photo)
    val recycler: RecyclerView by bindView(R.id.image_recycler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
    }

}