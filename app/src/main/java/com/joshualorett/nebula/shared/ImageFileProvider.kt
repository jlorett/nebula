package com.joshualorett.nebula.shared

import android.net.Uri
import androidx.core.content.FileProvider

/**
 * Workaround for the Glide cache not having file extensions on files. To use, provide this in the
 * Manifest instead of the normal [FileProvider].
 *
 * Created by Joshua on 3/14/2020.
 */
class ImageFileProvider : FileProvider() {
    override fun getType(uri: Uri): String? {
        return "image/*"
    }
}
