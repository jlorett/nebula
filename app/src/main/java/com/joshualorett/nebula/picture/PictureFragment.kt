package com.joshualorett.nebula.picture


import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.joshualorett.nebula.NasaRetrofitClient

import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.shared.GlideImageCache
import com.joshualorett.nebula.shared.ImageCache
import kotlinx.android.synthetic.main.fragment_picture.*
import kotlinx.coroutines.Dispatchers
import java.io.File

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
        val viewModel = ViewModelProvider(this, PictureViewModelFactory(repo, id)).get(PictureViewModel::class.java)
        viewModel.picture.observe(this, Observer { url ->
            pictureError.visibility = View.GONE
            apodPicture.visibility = View.VISIBLE
            Glide.with(this)
                .download(GlideUrl(url))
                .into(object: CustomViewTarget<SubsamplingScaleImageView, File>(apodPicture) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        apodPicture.visibility = View.GONE
                        pictureError.visibility = View.VISIBLE
                        pictureError.text = getString(R.string.picture_error)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {}

                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        apodPicture.setImage(ImageSource.uri(Uri.fromFile(resource)))
                    }
                })
        })
        viewModel.error.observe(this, Observer { errorMessage ->
            apodPicture.visibility = View.GONE
            pictureError.text = errorMessage
        })
    }

    override fun onDestroy() {
        imageCache.detachApplicationContext()
        super.onDestroy()
    }
}
