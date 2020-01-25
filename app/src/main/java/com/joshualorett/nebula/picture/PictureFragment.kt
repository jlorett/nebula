package com.joshualorett.nebula.picture


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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

    companion object {
        private const val apodId = "apodId"

        fun getInstance(id: Long): PictureFragment {
            val fragment = PictureFragment()
            val args = Bundle()
            args.putLong(apodId, id)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_picture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            getString(R.string.key)
        )
        val apodDao = ApodDatabaseProvider.getDatabase(requireContext().applicationContext).apodDao()
        imageCache = GlideImageCache(Dispatchers.Default)
        imageCache.attachApplicationContext(requireContext().applicationContext)
        val repo = ApodRepository(dataSource, apodDao, imageCache)

        val id = arguments?.getLong(apodId) ?: 0L
        val viewModel = ViewModelProviders.of(this, PictureViewModelFactory(repo, id)).get(PictureViewModel::class.java)
        viewModel.picture.observe(this, Observer { url ->
            pictureError.visibility = View.GONE
            apodPicture.visibility = View.VISIBLE
            Glide.with(this)
                .load(url)
                .transform(FullScreen(pictureContainer.width, pictureContainer.height))
                .into(apodPicture)
        })
        viewModel.error.observe(this, Observer { errorMessage ->
            apodPicture.visibility = View.GONE
            pictureError.text = errorMessage
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        imageCache.detachApplicationContext()
    }
}
