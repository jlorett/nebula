package com.joshualorett.nebula

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.OneShotEventObserver
import com.joshualorett.nebula.today.TodayViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            getString(R.string.key)
        )
        val apodDao = ApodDatabaseProvider.getDatabase(applicationContext).apodDao()
        val repo = ApodRepository(dataSource, apodDao)

        val viewModel = ViewModelProviders.of(this,
            TodayViewModel.TodayViewModelFactory(repo)).get(TodayViewModel::class.java)
        viewModel.apod.observe(this, Observer { apod ->
            val isPhoto = apod.mediaType == "image"
            pictureTitle.text = apod.title
            pictureDescription.text = apod.explanation
            if(apod.copyright == null) {
                copyright.visibility = View.GONE
            } else {
                copyright.visibility = View.VISIBLE
                val copyrightText = getString(R.string.today_copyright, apod.copyright)
                copyright.text = Html.fromHtml(copyrightText, Html.FROM_HTML_MODE_LEGACY)
            }
            if(isPhoto) {
                videoLinkBtn.hide()
                Glide.with(this)
                    .load(apod.url)
                    .transition(withCrossFade())
                    .into(picture)
            } else {
                videoLinkBtn.show()
            }
        })
        viewModel.error.observe(this, Observer { error ->
            pictureTitle.text = getString(R.string.today_error)
            pictureDescription.text = error
        })
        viewModel.loading.observe(this, Observer { loading ->
            if(loading) {
                pictureTitle.text = getString(R.string.today_loading)
                pictureDescription.text = ""
            }
        })
        viewModel.navigateVideoLink.observe(this, OneShotEventObserver { url ->
            url?.let {
                navigateToLink(it)
            }
        })

        videoLinkBtn.setOnClickListener { view ->
            viewModel.videoLinkClicked()
        }
        videoLinkBtn.hide()
    }

    private fun navigateToLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
