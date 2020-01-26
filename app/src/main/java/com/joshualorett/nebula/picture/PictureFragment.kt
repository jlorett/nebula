package com.joshualorett.nebula.picture


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.joshualorett.nebula.NasaRetrofitClient

import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.shared.GlideImageCache
import com.joshualorett.nebula.shared.ImageCache
import kotlinx.android.synthetic.main.fragment_picture.*
import kotlinx.coroutines.Dispatchers

/**
 * A full screen view of an [Apod] picture.
 */
class PictureFragment : Fragment() {
    private lateinit var imageCache: ImageCache
    private val args: PictureFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_picture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.pictureStatusBar)
        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            getString(R.string.key)
        )
        val apodDao = ApodDatabaseProvider.getDatabase(requireContext().applicationContext).apodDao()
        imageCache = GlideImageCache(Dispatchers.Default)
        imageCache.attachApplicationContext(requireContext().applicationContext)
        val repo = ApodRepository(dataSource, apodDao, imageCache)

        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(pictureToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appCompatActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
        pictureToolbar?.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        val id = args.id
        val viewModel = ViewModelProviders.of(this, PictureViewModelFactory(repo, id)).get(PictureViewModel::class.java)
        viewModel.picture.observe(this, Observer { url ->
            pictureError.visibility = View.GONE
            apodPicture.visibility = View.VISIBLE
            Glide.with(this)
                .load(url)
                .centerInside()
                .into(apodPicture)
        })
        viewModel.error.observe(this, Observer { errorMessage ->
            apodPicture.visibility = View.GONE
            pictureError.text = errorMessage
        })
    }

    override fun onDestroy() {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        imageCache.detachApplicationContext()
        super.onDestroy()
    }
}
