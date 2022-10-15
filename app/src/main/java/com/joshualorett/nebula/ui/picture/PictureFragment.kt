package com.joshualorett.nebula.ui.picture

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.databinding.FragmentPictureBinding
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * A full screen view of an [Apod] picture.
 */
@AndroidEntryPoint
class PictureFragment : Fragment(R.layout.fragment_picture) {
    private var imageUri: Uri? = null
    private val viewModel: PictureViewModel by viewModels()
    @Inject lateinit var imageCache: ImageCache
    private var pictureBinding: FragmentPictureBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPictureBinding.bind(view)
        pictureBinding = binding
        binding.pictureToolbar.title = ""
        binding.pictureToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.pictureToolbar.inflateMenu(R.menu.picture)
        binding.pictureToolbar.setOnMenuItemClickListener(object :
                Toolbar.OnMenuItemClickListener,
                androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
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
        binding.apodPicture.setDoubleTapZoomScale(1.4f)
        binding.apodPicture.setDoubleTapZoomDuration(
            resources
                .getInteger(android.R.integer.config_shortAnimTime)
        )
        imageCache.attachApplicationContext(requireContext().applicationContext)
        viewModel.picture.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { resource ->
                processResource(binding, resource)
            }
            .launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pictureBinding = null
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

    private fun processResource(
        binding: FragmentPictureBinding,
        resource: Resource<Apod, String>
    ) {
        when (resource) {
            is Resource.Success -> {
                val url = resource.data.hdurl ?: resource.data.url
                if (url.isEmpty()) {
                    showError(binding, getString(R.string.error_empty_url))
                } else {
                    updateImage(binding, url)
                }
            }
            is Resource.Error -> {
                showError(binding, getString(R.string.error_fetching))
            }
            Resource.Loading -> {
                // no-op
            }
        }
    }

    private fun updateImage(
        binding: FragmentPictureBinding,
        url: String
    ) {
        binding.pictureError.visibility = View.GONE
        binding.apodPicture.visibility = View.VISIBLE
        Glide.with(this)
            .asFile()
            .load(GlideUrl(url))
            .into(object : CustomViewTarget<SubsamplingScaleImageView, File>(binding.apodPicture) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    showError(binding, getString(R.string.picture_error))
                }

                override fun onResourceCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    imageUri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider", resource
                    )
                    imageUri?.let {
                        preparePictureAnimation(binding)
                        lifecycleScope.launch {
                            animatePicture(binding)
                        }
                        binding.apodPicture.setImage(ImageSource.uri(it))
                    }
                }
            })
    }

    private fun showError(
        binding: FragmentPictureBinding,
        error: String
    ) {
        prepareErrorAnimation(binding)
        binding.apodPicture.visibility = View.GONE
        binding.pictureError.visibility = View.VISIBLE
        binding.pictureError.text = error
        animateError(binding)
    }
}
