package com.joshualorett.nebula.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.joshualorett.nebula.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.shared.GlideImageCache
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.today.TodaySyncManager
import kotlinx.android.synthetic.main.fragment_settings_container.*
import kotlinx.coroutines.Dispatchers

class SettingsContainerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_container, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.settings, SettingsFragment(), SettingsFragment::class.java.simpleName)
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var syncPreference: SwitchPreferenceCompat? = null
        private var unmeteredPreference: SwitchPreferenceCompat? = null

        private lateinit var syncKey: String
        private lateinit var unmeteredKey: String

        private lateinit var imageCache: ImageCache
        private lateinit var settingsViewModel: SettingsViewModel

        private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when(key) {
                    syncKey -> {
                        if(syncPreference?.isChecked == true) {
                            TodaySyncManager.setRecurringSyncAlarm(requireContext())
                        } else {
                            TodaySyncManager.cancelRecurringSyncAlarm(requireContext())
                        }
                        updateUnmeteredState()
                    }
                }
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)
            syncKey = getString(R.string.settings_key_sync)
            unmeteredKey = getString(R.string.settings_key_unmetered)
            val clearDataKey = getString(R.string.settings_key_clear)

            syncPreference = findPreference(syncKey)
            syncPreference?.setOnPreferenceClickListener {
                syncPreference?.isChecked = syncPreference?.isChecked ?: false
                updateUnmeteredState()
                true
            }
            unmeteredPreference = findPreference(unmeteredKey)
            updateUnmeteredState()
            val clearDataPreference: Preference? = findPreference(clearDataKey)
            clearDataPreference?.setOnPreferenceClickListener {
                showClearDataDialog()
                true
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val aboutPref = findPreference<Preference>(getString(R.string.settings_key_about))
            aboutPref?.let {
                val version = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
                it.summary = version
            }
            imageCache = GlideImageCache(Dispatchers.Default)
            imageCache.attachApplicationContext(requireContext().applicationContext)
            val dataSource = ApodRemoteDataSource(
                NasaRetrofitClient,
                getString(R.string.key)
            )
            val apodDao = ApodDatabaseProvider.getDatabase(requireContext().applicationContext).apodDao()
            val apodRepository = ApodRepository(dataSource, apodDao, imageCache)
            settingsViewModel = ViewModelProvider(viewModelStore, SettingsViewModel.SettingsViewModelFactory(apodRepository)).get(SettingsViewModel::class.java)
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        }

        override fun onPause() {
            super.onPause()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }

        override fun onDestroy() {
            imageCache.detachApplicationContext()
            super.onDestroy()
        }

        private fun updateUnmeteredState() {
            val syncOn = syncPreference?.isChecked == true
            if(!syncOn) {
                unmeteredPreference?.isChecked = false
            }
            unmeteredPreference?.isVisible = syncOn
        }

        private fun showClearDataDialog() {
            val dialog = ClearDataDialogFragment.create()
            dialog.setListener(object: ClearDataDialogFragment.Listener {
                override fun onClear() {
                    settingsViewModel.clearData()
                }
            })
            dialog.show(parentFragmentManager, "clearDataDialog")
        }
    }
}