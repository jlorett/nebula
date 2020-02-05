package com.joshualorett.nebula.today

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.joshualorett.nebula.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.shared.GlideImageCache
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.OneShotEventObserver
import kotlinx.android.synthetic.main.fragment_picture.*
import kotlinx.android.synthetic.main.fragment_today_photo.*
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        todayCollapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(pictureToolbar)

        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            getString(R.string.key)
        )
        val apodDao = ApodDatabaseProvider.getDatabase(requireContext().applicationContext).apodDao()
        val repo = ApodRepository(dataSource, apodDao, imageCache)

        val viewModel = ViewModelProvider(this, TodayViewModel.TodayViewModelFactory(repo)).get(TodayViewModel::class.java)
        viewModel.apod.observe(this, Observer { apod ->
            updateApod(apod)
        })
        viewModel.error.observe(this, Observer { error ->
            todayTitle.text = getString(R.string.today_error)
            todayDescription.text = error
            todayPicture.visibility = View.GONE
        })
        viewModel.loading.observe(this, Observer { loading ->
            todayProgressBar.visibility = if(loading) View.VISIBLE else View.GONE
        })
        viewModel.navigateVideoLink.observe(this, OneShotEventObserver { url ->
            url?.let {
                navigateToLink(it)
            }
        })
        viewModel.navigateFullPicture.observe(this, OneShotEventObserver { id ->
            val action = TodayPhotoFragmentDirections.actionTodayPhotoFragmentToPictureFragment(id)
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
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
    }

    override fun onDestroy() {
        imageCache.detachApplicationContext()
        super.onDestroy()
    }

    private fun navigateToLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun updateApod(apod: Apod) {
        val isPhoto = apod.mediaType == "image"
        todayDate.text = getFormattedDate(apod.date)
        todayTitle.text = apod.title
        todayDescription.text = apod.explanation
        if(apod.copyright == null) {
            todayCopyright.visibility = View.INVISIBLE
        } else {
            todayCopyright.visibility = View.VISIBLE
            val copyrightText = getString(R.string.today_copyright, apod.copyright)
            todayCopyright.text = Html.fromHtml(copyrightText, Html.FROM_HTML_MODE_LEGACY)
        }
        if(isPhoto) {
            todayVideoLinkBtn.hide()
            apod.hdurl?.let {
                Glide.with(this)
                    .download(GlideUrl(apod.hdurl))
                    .preload()
            }
            Glide.with(this)
                .load(apod.url)
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

    private fun getFormattedDate(date: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy MMMM dd")
            LocalDate.parse(date).format(formatter)
        } catch (e: Exception) {
            date
        }
    }
}
