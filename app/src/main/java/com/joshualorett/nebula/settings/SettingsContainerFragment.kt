package com.joshualorett.nebula.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.joshualorett.nebula.R
import kotlinx.android.synthetic.main.fragment_settings_container.*

class SettingsContainerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_container, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        settingsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.settings, SettingsFragment(), SettingsFragment::class.java.simpleName)
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val aboutPref = findPreference<Preference>("pref_key_about")
            aboutPref?.let {
                val version = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
                it.summary = version
            }
        }
    }
}