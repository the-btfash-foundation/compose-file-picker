package sh.tablet.android.compose.filepicker

import android.icu.text.DecimalFormat
import okhttp3.internal.format
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.math.log10
import kotlin.math.pow

fun getMime(file: File): String {
	val path = file.toPath()
	val mime: String? = try {
		Files.probeContentType(path)
	} catch (e: IOException) {
		e.printStackTrace()
		null
	}
	return mime ?: "unknown/type"
}

fun isPreviewableMedia(file: File): Boolean {
	return listOf("image", "video", "audio").contains(getMime(file).split("/")[0])
}

fun readableFileSize(size: Long, includeSpace: Boolean = false): String {
	if(size <= 0) return "0B"
	val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
	var digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
	if(digitGroups >= units.size) {
		digitGroups = units.size - 1
	}
	val spc = if(includeSpace) {
		" "
	} else {
		""
	}
	return format("%s%s%s", DecimalFormat("#,##0.#").format(size / 1024.toDouble().pow(digitGroups)).toString(), spc, units[digitGroups])
}