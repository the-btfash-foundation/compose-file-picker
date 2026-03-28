package sh.tablet.android.compose.filepicker.viewmodel

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString


internal fun sortByToComparator(
	sortBy: FilePickerState.SortBy,
	direction: FilePickerState.SortDirection
): Comparator<Path> {
	val cmp: Comparator<Path> = when (sortBy) {
		FilePickerState.SortBy.DATE -> {
			compareBy { p ->
				val attrs =
					Files.getFileAttributeView<BasicFileAttributeView>(p, BasicFileAttributeView::class.java)
						.readAttributes()
				attrs.lastModifiedTime().toMillis()
			}
		}

		FilePickerState.SortBy.NAME -> {
			compareBy { p ->
				p.fileName.name
			}
		}

		FilePickerState.SortBy.SIZE -> {
			compareBy { p ->
				if (p.isDirectory()) {
					p.listDirectoryEntries().size.toLong()
				} else {
					p.fileSize()
				}
			}
		}

		FilePickerState.SortBy.TYPE -> {
			compareBy { p ->
				if (p.isDirectory()) {
					"<dir>"
				} else {
					p.extension
				}
			}
		}
	}
	if (direction == FilePickerState.SortDirection.DESCENDING) {
		return cmp.reversed()
	}
	return cmp
}

class FilesystemPagingSource(private val pickerState: FilePickerState) :
	PagingSource<Long, Path>() {
	private val offscreenBuffer: Long = 5

	override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Path> {
		val pageIndex = params.key ?: 1
		val pageSize = params.loadSize
		return try {
			withContext(Dispatchers.IO) {
				Files.newDirectoryStream(pickerState.currentDirectory)
			}.use { strm ->
				Log.d("pickerstate", pickerState.toString())
				val inter = strm.toList()
				val second =
					inter.sortedWith(sortByToComparator(pickerState.sortBy, pickerState.sortDirection))
						.filter { p ->
							var cond = true

							if (pickerState.config.onlyDirectories) {
								cond = p.isDirectory()
							}
							cond = cond && FileSystems.getDefault()
								.getPathMatcher("glob:${pickerState.config.pattern}").matches(p.fileName)
							cond
						}
						.filter { p ->
							if (pickerState.currentFilter.isBlank())
								true
							else {
								Log.d("filename", p.fileName.name)
								p.fileName.name
									.matches(
										Regex(
											".*${Regex.escape(pickerState.currentFilter)}.*",
											RegexOption.IGNORE_CASE
										)
									)
							}
						}.filter { p ->
							if (pickerState.showHidden)
								true
							else !p.isHidden()
						}
				val third = second.filter {
					it.isDirectory()
				} + second.filter {
					!it.isDirectory()
				}
				val stuff = third.subList(
					((pageIndex - 1) * pageSize).coerceAtMost(third.size.toLong()).toInt(),
					(pageIndex * pageSize).coerceAtMost(third.size.toLong()).toInt()
				)

				Log.d(
					"SOURCE",
					"filt='${pickerState.currentFilter}' stuff=${third.size}; orig=${inter.size}"
				)
				LoadResult.Page(
					stuff,
					if (pageIndex == 1L) null else pageIndex - 1,
					nextKey = if (stuff.isEmpty() || (pageIndex) * pageSize.toLong() >= third.size) null else pageIndex + 1
				)
			}
		} catch (e: Exception) {
			e.printStackTrace()
			LoadResult.Error(e)
		}
	}

	override fun getRefreshKey(state: PagingState<Long, Path>): Long? {
		return state.anchorPosition?.let { anchor ->
			state.closestPageToPosition(anchor)?.prevKey?.plus(offscreenBuffer)
				?: state.closestPageToPosition(anchor)?.nextKey?.minus(offscreenBuffer)
		}
	}
}