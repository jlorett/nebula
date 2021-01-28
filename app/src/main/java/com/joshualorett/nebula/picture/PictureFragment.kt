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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.joshualorett.nebula.R
import com.joshualorett.nebula.databinding.FragmentPictureBinding
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * A full screen view of an [Apod] picture.
 */
@AndroidEntryPoint
class PictureFragment : Fragment() {
    private val args: PictureFragmentArgs by navArgs()
    private var imageUri: Uri? = null
    private val viewModel: PictureViewModel by viewModels()
    @Inject lateinit var imageCache: ImageCache

    private var _binding: FragmentPictureBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pictureToolbar.title = ""
        binding.pictureToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.pictureToolbar.inflateMenu(R.menu.picture)
        binding.pictureToolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener,
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
        binding.apodPicture.setDoubleTapZoomScale(1.4f)
        binding.apodPicture.setDoubleTapZoomDuration(resources.getInteger(android.R.integer.config_shortAnimTime))
        imageCache.attachApplicationContext(requireContext().applicationContext)
        if(savedInstanceState == null) {
            val id = args.id
            viewModel.load(id)
        }
        viewModel.picture.observe(viewLifecycleOwner, Observer { resource ->
            when(resource) {
                is Resource.Success -> {
                    val url = resource.data.hdurl ?: resource.data.url
                    if(url.isEmpty()) {
                        showError(getString(R.string.error_empty_url))
                    } else {
                        updateImage(url)
                    }
                }
                is Resource.Error -> {
                    showError(getString(R.string.error_fetching))
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun updateImage(url: String) {
        binding.pictureError.visibility = View.GONE
        binding.apodPicture.visibility = View.VISIBLE
        Glide.with(this)
            .asFile()
            .load(GlideUrl(url))
            .into(object: CustomViewTarget<SubsamplingScaleImageView, File>(binding.apodPicture) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    showError(getString(R.string.picture_error))
                }

                override fun onResourceCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", resource)
                    imageUri?.let {
                        preparePictureAnimation()
                        lifecycleScope.launch {
                            animatePicture()
                        }
                        binding.apodPicture.setImage(ImageSource.uri(it))
                    }
                }
            })
    }

    private fun showError(error: String) {
        prepareErrorAnimation()
        binding.apodPicture.visibility = View.GONE
        binding.pictureError.visibility = View.VISIBLE
        binding.pictureError.text = error
        animateError()
    }
}