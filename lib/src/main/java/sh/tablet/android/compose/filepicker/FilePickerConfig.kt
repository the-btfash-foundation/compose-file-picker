package sh.tablet.android.compose.filepicker

import android.os.Environment
import java.time.format.DateTimeFormatter

/**
 * Configuration for the [sh.tablet.android.compose.filepicker.FilePicker] composable.
 */
data class FilePickerConfig(
	val pattern: String = "*",
	val onlyDirectories: Boolean = false,
	val showThumbnails: Boolean = false,
	val initialDirectory: String = Environment.getExternalStorageDirectory().absolutePath,
	val allowMultiple: Boolean = false,
	/**
	 * A [DateTimeFormatter]-compatible pattern with which the last write time is displayed
	 */
	val dateModifiedFormat: String = "yyyy-MM-dd hh:mm:ss a",

) {
	fun getMTimeFormatter(): DateTimeFormatter {
		return DateTimeFormatter.ofPattern(dateModifiedFormat)
	}
}

