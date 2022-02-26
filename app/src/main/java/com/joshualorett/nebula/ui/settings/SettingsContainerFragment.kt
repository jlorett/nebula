package com.joshualorett.nebula.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.joshualorett.nebula.R
import com.joshualorett.nebula.databinding.FragmentSettingsContainerBinding
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.ui.today.sync.cancelRecurringSyncWork
import com.joshualorett.nebula.ui.today.sync.setupRecurringSyncWork
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsContainerFragment : Fragment(R.layout.fragment_settings_container) {
    private var settingsBinding: FragmentSettingsContainerBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingsContainerBinding.bind(view)
        settingsBinding = binding
        binding.settingsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.settings, SettingsFragment(), SettingsFragment::class.java.simpleName)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        settingsBinding = null
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {
        private var syncPreference: SwitchPreferenceCompat? = null
        private var unmeteredPreference: SwitchPreferenceCompat? = null
        private lateinit var syncKey: String
        private val settingsViewModel: SettingsViewModel by viewModels()
        @Inject lateinit var imageCache: ImageCache

        private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    syncKey -> {
                        if (syncPreference?.isChecked == true) {
                            setupRecurringSyncWork(requireContext().applicationContext)
                        } else {
                            cancelRecurringSyncWork(requireContext().applicationContext)
                        }
                        updateUnmeteredState()
                    }
                }
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)
            syncKey = getString(R.string.settings_key_sync)
            val unmeteredKey = getString(R.string.settings_key_unmetered)
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
                val version = requireActivity()
                    .packageManager
                    .getPackageInfo(requireActivity().packageName, 0)
                    .versionName
                it.summary = version
            }
            imageCache.attachApplicationContext(requireContext().applicationContext)
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
            if (!syncOn) {
                unmeteredPreference?.isChecked = false
            }
            unmeteredPreference?.isVisible = syncOn
        }

        private fun showClearDataDialog() {
            val dialog = ClearDataDialogFragment.create()
            dialog.setListener(object : ClearDataDialogFragment.Listener {
                override fun onClear() {
                    settingsViewModel.clearData()
                }
            })
            dialog.show(parentFragmentManager, "clearDataDialog")
        }
    }
}
