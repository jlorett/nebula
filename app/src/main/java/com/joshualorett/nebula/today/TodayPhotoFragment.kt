package com.joshualorett.nebula.today

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.formattedDate
import com.joshualorett.nebula.apod.hasImage
import com.joshualorett.nebula.date.ApodDatePickerFactory
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.OneShotEventObserver
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_today_photo.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Displays Today's [Apod].
 */
@AndroidEntryPoint
class TodayPhotoFragment : Fragment() {
    @Inject lateinit var imageCache: ImageCache
    private val viewModel: TodayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageCache.attachApplicationContext(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.apod.observe(viewLifecycleOwner, Observer { resource ->
            when(resource) {
                is Resource.Success -> {
                    todaySwipeRefreshLayout.isRefreshing = false
                    updateApod(resource.data)
                }
                is Resource.Loading -> {
                    if(!todaySwipeRefreshLayout.isRefreshing) {
                        todaySwipeRefreshLayout.isRefreshing = true
                    }
                }
                is Resource.Error -> {
                    val error = resource.error
                    todaySwipeRefreshLayout.isRefreshing = false
                    todayToolbar.title = getString(R.string.app_name)
                    todayCollapsingToolbar.isTitleEnabled = false
                    todayTitle.text = getString(R.string.today_error)
                    todayDescription.text = error
                    todayPicture.visibility = View.GONE
                    todayCopyright.text = ""
                    todayCopyright.visibility = View.INVISIBLE
                    todayDate.text = ""
                    todayVideoLinkBtn.hide()
                }
            }
        })
        viewModel.navigateVideoLink.observe(viewLifecycleOwner, OneShotEventObserver { url ->
            url?.let {
                navigateToLink(it)
            }
        })
        viewModel.navigateFullPicture.observe(viewLifecycleOwner, OneShotEventObserver { id ->
            val action = TodayPhotoFragmentDirections.actionTodayPhotoFragmentToPictureFragment(id)
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        })

        todayCollapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        todayToolbar.inflateMenu(R.menu.today)
        todayToolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener,
            androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return when(item?.itemId) {
                    R.id.action_refresh -> {
                        viewModel.refresh()
                        true
                    }
                    R.id.action_settings -> {
                        navigateToSettings()
                        true
                    }
                    R.id.action_choose_day -> {
                        showDatePicker()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        })
        todayVideoLinkBtn.setOnClickListener {
            viewModel.videoLinkClicked()
        }
        todayVideoLinkBtn.hide()

        todayContainer.doOnPreDraw {
            val isPortrait = it.height >= it.width
            todayPicture.layoutParams.height = if(isPortrait) it.width / 4 * 3 else it.height
        }
        todayPicture.setOnClickListener {
            viewModel.onPhotoClicked()
        }
        todaySwipeRefreshLayout.apply {
            setColorSchemeResources(R.color.colorSecondary)
            setProgressBackgroundColorSchemeResource(R.color.colorSurface)
            setOnRefreshListener {
                viewModel.refresh()
            }
        }
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
        val action = TodayPhotoFragmentDirections.actionTodayPhotoFragmentToSettingsContainerFragment()
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
    }

    private fun showDatePicker() {
        val datePicker = ApodDatePickerFactory.create(viewModel.currentDate() ?: LocalDate.now())
        datePicker.addOnPositiveButtonClickListener { selection: Long ->
            viewModel.updateDate(Instant.ofEpochMilli(selection).atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toLocalDate())
        }
        datePicker.show(parentFragmentManager, "datePicker")
    }

    private fun updateApod(apod: Apod) {
        prepareApodAnimation()
        todayDate.text = apod.formattedDate("yyyy MMMM dd")
        todayTitle.text = apod.title
        todayDescription.text = apod.explanation
        if(apod.copyright == null) {
            todayCopyright.visibility = View.INVISIBLE
        } else {
            todayCopyright.visibility = View.VISIBLE
            todayCopyright.text = getString(R.string.today_copyright, apod.copyright)
        }
        if(apod.hasImage()) {
            todayVideoLinkBtn.hide()
            Glide.with(this)
                .load(apod.hdurl ?: apod.url)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(todayPicture)
        } else {
            todayPicture.visibility = View.GONE
            todayToolbar.title = getString(R.string.app_name)
            todayCollapsingToolbar.isTitleEnabled = false
            todayVideoLinkBtn.show()
        }
        animateApod()
    }

    private fun prepareApodAnimation() {
        todayDate.alpha = 0F
        todayTitle.alpha = 0F
        todayDescription.alpha = 0F
        todayCopyright.alpha = 0F
    }

    private fun animateApod() {
        val interpolator = LinearOutSlowInInterpolator()
        val duration = 300L
        todayDate.animate()
            .alpha(1F)
            .setInterpolator(interpolator)
            .setDuration(duration)
        todayTitle.animate()
            .alpha(1F)
            .setInterpolator(interpolator)
            .setDuration(duration)
        todayDescription.animate()
            .alpha(1F)
            .setInterpolator(interpolator)
            .setDuration(duration)
        todayCopyright.animate()
            .alpha(1F)
            .setInterpolator(interpolator)
            .setDuration(duration)
    }
}
