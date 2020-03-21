package com.joshualorett.nebula.picture


import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
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
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_picture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pictureToolbar.title = ""
        pictureToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        pictureToolbar.inflateMenu(R.menu.picture)
        pictureToolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener,
            androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return when(item?.itemId) {
                    R.id.action_share_image -> {
                        shareImage()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        })
        apodPicture.setDoubleTapZoomScale(1.2f)
        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            getString(R.string.key)
        )
        val apodDao = ApodDatabaseProvider.getDatabase(requireContext().applicationContext).apodDao()
        imageCache = GlideImageCache(Dispatchers.Default)
        imageCache.attachApplicationContext(requireContext().applicationContext)
        val repo = ApodRepository(dataSource, apodDao, imageCache)

        val id = args.id
        val viewModel = ViewModelProvider(this, PictureViewModelFactory(repo, id)).get(PictureViewModel::class.java)
        viewModel.picture.observe(viewLifecycleOwner, Observer { url ->
            pictureError.visibility = View.GONE
            apodPicture.visibility = View.VISIBLE
            Glide.with(this)
                .asFile()
                .load(GlideUrl(url))
                .into(object: CustomViewTarget<SubsamplingScaleImageView, File>(apodPicture) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        apodPicture.visibility = View.GONE
                        pictureError.visibility = View.VISIBLE
                        pictureError.text = getString(R.string.picture_error)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {}

                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", resource)
                        imageUri?.let {
                            apodPicture.setImage(ImageSource.uri(it))
                        }
                    }
                })
        })
        viewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            apodPicture.visibility = View.GONE
            pictureError.text = errorMessage
        })
    }

    override fun onDestroy() {
        imageUri = null
        imageCache.detachApplicationContext()
        super.onDestroy()
    }

    private fun shareImage() {
        imageUri?.let { img ->
            val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
                .setStream(img)
                .setType("image/jpg")
                .intent
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val chooser = Intent.createChooser(shareIntent, resources.getText(R.string.share))
            startActivity(chooser)
        }
    }
}