package com.pitchedapps.frost.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.postDelayed
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.ARG_IMAGE_URL
import com.pitchedapps.frost.utils.ARG_TEXT
import com.pitchedapps.frost.utils.GlideApp
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-07-15.
 */
class ImageActivity : AppCompatActivity() {

    //    val panel: SlidingUpPanelLayout by bindView(R.id.image_panel)
    val photo: ImageView by bindView(R.id.image_photo)
//    val recycler: RecyclerView by bindView(R.id.image_recycler)

    val imageUrl: String
        get() = intent.extras.getString(ARG_IMAGE_URL)

    val text: String?
        get() = intent.extras.getString(ARG_TEXT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image2)
        L.d("Load Image Activity", imageUrl)
        postDelayed(1000) {
            GlideApp.with(this).load("https://avatars5.githubusercontent.com/u/2906988?v=4&s=400").placeholder(R.drawable.frost_f_256).into(photo)
        }
    }


}