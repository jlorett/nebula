package com.joshualorett.nebula.ui.today

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.formattedDate
import com.joshualorett.nebula.apod.hasImage
import com.joshualorett.nebula.databinding.FragmentTodayPhotoBinding
import com.joshualorett.nebula.date.ApodDatePickerFactory
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Displays Today's [Apod].
 */
@AndroidEntryPoint
class TodayPhotoFragment : Fragment(R.layout.fragment_today_photo) {
    @Inject lateinit var imageCache: ImageCache
    private val viewModel: TodayViewModel by viewModels()
    private var todayBinding: FragmentTodayPhotoBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageCache.attachApplicationContext(requireContext().applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTodayPhotoBinding.bind(view)
        todayBinding = binding
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apod.collect { resource ->
                        processResource(binding, resource)
                    }
                }
                launch {
                    viewModel.navigateFullPicture.collect { id ->
                        val action = TodayPhotoFragmentDirections
                            .actionTodayPhotoFragmentToPictureFragment(id)
                        requireActivity()
                            .findNavController(R.id.nav_host_fragment)
                            .navigate(action)
                    }
                }
                launch {
                    viewModel.navigateVideoLink.collect { url ->
                        url?.let {
                            navigateToLink(it)
                        }
                    }
                }
                launch {
                    viewModel.showDatePicker.collect { date ->
                        showDatePicker(date)
                    }
                }
            }
        }
        binding.todayCollapsingToolbar.setExpandedTitleColor(
            ContextCompat.getColor(requireContext(), android.R.color.transparent)
        )
        binding.todayToolbar.inflateMenu(R.menu.today)
        binding.todayToolbar.setOnMenuItemClickListener(object :
                Toolbar.OnMenuItemClickListener,
                androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
                        R.id.action_refresh -> {
                            viewModel.refresh()
                            true
                        }
                        R.id.action_settings -> {
                            navigateToSettings()
                            true
                        }
                        R.id.action_choose_day -> {
                            viewModel.onChooseDate()
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
            })
        binding.todayVideoLinkBtn.setOnClickListener {
            viewModel.videoLinkClicked()
        }
        binding.todayVideoLinkBtn.hide()
        binding.todayContainer.doOnPreDraw {
            val isPortrait = it.height >= it.width
            binding.todayPicture.layoutParams.height =
                if (isPortrait) it.width / 4 * 3
                else it.height
        }
        binding.todayPicture.setOnClickListener {
            viewModel.onPhotoClicked()
        }
        binding.todaySwipeRefreshLayout.apply {
            setColorSchemeResources(R.color.colorSecondary)
            setProgressBackgroundColorSchemeResource(R.color.colorSurface)
            setOnRefreshListener {
                viewModel.refresh()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        todayBinding = null
    }

    override fun onDestroy() {
        imageCache.detachApplicationContext()
        super.onDestroy()
    }

    private fun navigateToLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val action = TodayPhotoFragmentDirections
            .actionTodayPhotoFragmentToSettingsContainerFragment()
        requireActivity()
            .findNavController(R.id.nav_host_fragment)
            .navigate(action)
    }

    private fun showDatePicker(date: LocalDate) {
        val datePicker = ApodDatePickerFactory.create(date)
        datePicker.addOnPositiveButtonClickListener { selection: Long ->
            viewModel.updateDate(
                Instant
                    .ofEpochMilli(selection)
                    .atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC))
                    .toLocalDate()
            )
        }
        datePicker.show(parentFragmentManager, "datePicker")
    }

    private fun processResource(
        binding: FragmentTodayPhotoBinding,
        resource: Resource<Apod, String>
    ) {
        when (resource) {
            is Resource.Success -> {
                showApod(binding, resource.data)
            }
            is Resource.Loading -> {
                if (!binding.todaySwipeRefreshLayout.isRefreshing) {
                    binding.todaySwipeRefreshLayout.isRefreshing = true
                }
            }
            is Resource.Error -> {
                val error = resource.error
                showError(binding, error)
            }
        }
    }

    private fun showError(
        binding: FragmentTodayPhotoBinding,
        error: String
    ) {
        prepareErrorAnimation(binding)
        binding.todaySwipeRefreshLayout.isRefreshing = false
        binding.todayToolbar.title = getString(R.string.app_name)
        binding.todayCollapsingToolbar.isTitleEnabled = false
        binding.todayTitle.text = getString(R.string.today_error)
        binding.todayDescription.text = error
        binding.todayPicture.visibility = View.GONE
        binding.todayCopyright.text = ""
        binding.todayCopyright.visibility = View.INVISIBLE
        binding.todayDate.text = ""
        binding.todayVideoLinkBtn.hide()
        animateError(binding)
    }

    private fun showApod(
        binding: FragmentTodayPhotoBinding,
        apod: Apod
    ) {
        prepareApodAnimation(binding)
        binding.todaySwipeRefreshLayout.isRefreshing = false
        binding.todayDate.text = apod.formattedDate("yyyy MMMM dd")
        binding.todayTitle.text = apod.title
        binding.todayDescription.text = apod.explanation
        if (apod.copyright == null) {
            binding.todayCopyright.visibility = View.INVISIBLE
        } else {
            binding.todayCopyright.visibility = View.VISIBLE
            binding.todayCopyright.text = getString(R.string.today_copyright, apod.copyright)
        }
        if (apod.hasImage()) {
            showImage(binding, apod.hdurl ?: apod.url)
        } else {
            showVideo(binding)
        }
        animateApod(binding)
    }

    private fun showImage(
        binding: FragmentTodayPhotoBinding,
        url: String
    ) {
        binding.todayToolbar.title = ""
        binding.todayPicture.visibility = View.VISIBLE
        binding.todayCollapsingToolbar.isTitleEnabled = true
        binding.todayVideoLinkBtn.hide()
        Glide.with(this)
            .load(url)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.todayPicture)
    }

    private fun showVideo(binding: FragmentTodayPhotoBinding) {
        binding.todayPicture.visibility = View.GONE
        binding.todayToolbar.title = getString(R.string.app_name)
        binding.todayCollapsingToolbar.isTitleEnabled = false
        binding.todayVideoLinkBtn.show()
    }
}
