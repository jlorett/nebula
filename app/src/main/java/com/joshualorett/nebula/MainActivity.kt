package com.joshualorett.nebula

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.joshualorett.nebula.apod.ApodDatabaseProvider
import com.joshualorett.nebula.apod.ApodNetworkDataSource
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.today.TodayViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataSource = ApodNetworkDataSource(NasaRetrofitClient, getString(R.string.key))
        val apodDao = ApodDatabaseProvider.getDatabase(applicationContext).apodDao()
        val repo = ApodRepository(dataSource, apodDao)

        val viewModel = ViewModelProviders.of(this,
            TodayViewModel.TodayViewModelFactory(repo)).get(TodayViewModel::class.java)
        viewModel.apod.observe(this, Observer { apod ->
            pictureTitle.text = apod.title
            pictureDescription.text = apod.explanation
            Glide.with(this)
                .load(apod.url)
                .transition(withCrossFade())
                .into(picture)
        })
        viewModel.error.observe(this, Observer { error ->
            pictureTitle.text = "Error"
            pictureDescription.text = error
        })
        viewModel.loading.observe(this, Observer { loading ->
            if(loading) {
                pictureTitle.text = "Loading"
                pictureDescription.text = ""
            }
        })
    }
}
