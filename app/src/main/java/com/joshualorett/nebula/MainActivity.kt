package com.joshualorett.nebula

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.joshualorett.nebula.apod.ApodNetworkDataSource
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.today.TodayViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataSource = ApodNetworkDataSource(NasaRetrofitClient, getString(R.string.key))
        val repo = ApodRepository(dataSource)

        val viewModel = ViewModelProviders.of(this,
            TodayViewModel.TodayViewModelFactory(repo)).get(TodayViewModel::class.java)
        viewModel.apod.observe(this, Observer { apod ->
            status.text = """${apod.title}
                |${apod.explanation}
            """.trimMargin()
        })
        viewModel.error.observe(this, Observer { error ->
            status.text = error
        })
        viewModel.loading.observe(this, Observer { loading ->
            status.text = "Loading"
        })
    }
}
