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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.joshualorett.nebula.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.apod.formattedDate
import com.joshualorett.nebula.apod.hasImage
import com.joshualorett.nebula.shared.GlideImageCache
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.OneShotEventObserver
import kotlinx.android.synthetic.main.fragment_today_photo.*
import kotlinx.coroutines.Dispatchers

/**
 * Displays Today's [Apod].
 */
class TodayPhotoFragment : Fragment() {
    private lateinit var imageCache: ImageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageCache = GlideImageCache(Dispatchers.Default)
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
        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            getString(R.string.key)
        )
        val apodDao = ApodDatabaseProvider.getDatabase(requireContext().applicationContext).apodDao()
        val repo = ApodRepository(dataSource, apodDao, imageCache)
        val viewModel = ViewModelProvider(this, TodayViewModel.TodayViewModelFactory(repo)).get(TodayViewModel::class.java)
        viewModel.apod.observe(viewLifecycleOwner, Observer { apod ->
            updateApod(apod)
        })
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            todayToolbar.title = getString(R.string.app_name)
            todayCollapsingToolbar.isTitleEnabled = false
            todayTitle.text = getString(R.string.today_error)
            todayDescription.text = error
            todayPicture.visibility = View.GONE
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            if(loading and !todaySwipeRefreshLayout.isRefreshing) {
                todaySwipeRefreshLayout.isRefreshing = true
            } else if (!loading) {
                todaySwipeRefreshLayout.isRefreshing = false
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
        todaySwipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
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

    private fun updateApod(apod: Apod) {
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
    }
}
