package sh.tablet.android.compose.filepicker.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import sh.tablet.android.compose.filepicker.FilePickerConfig
import java.nio.file.Path
import java.nio.file.Paths

@ExperimentalCoroutinesApi
class FilePickerViewModel : ViewModel() {
	private val _currentDirectory =
		MutableStateFlow(Paths.get(Environment.getExternalStorageDirectory().absolutePath))
	private val _currentFilter = MutableStateFlow("")
	private val _sort = MutableStateFlow(FilePickerState.SortBy.NAME)
	private val _sortDirection = MutableStateFlow(FilePickerState.SortDirection.ASCENDING)
	private val _showHidden = MutableStateFlow(true)

	private val _config = MutableStateFlow(FilePickerConfig())

	var currentDirectory: Path?
		get() = _currentDirectory.value
		set(value) {
			_currentDirectory.value = value
		}
	val currentDirectoryState = _currentDirectory.asStateFlow()
	val filterState = _currentFilter.asStateFlow()
	val sortState = _sort.asStateFlow()
	val sortDirectionState = _sortDirection.asStateFlow()
	val showHiddenState = _showHidden.asStateFlow()

	var config get() = _config.value
		set(value) {
			_config.value = value
		}
	fun updateDirectory(p: Path) {
		_currentDirectory.value = p
	}

	fun setFilter(v: String) {
		_currentFilter.value = v
	}
	fun setSort(v: FilePickerState.SortBy) {
		_sort.value = v
	}
	fun setSortDirection(v: FilePickerState.SortDirection) {
		_sortDirection.value = v
	}
	fun setShowHidden(v: Boolean) {
		_showHidden.value = v
	}

	val filePickerState: StateFlow<FilePickerState> = combine(
		_currentDirectory,
		_currentFilter,
		_sort,
		_sortDirection,
		_showHidden
	) { currentDirectory, currentFilter, sort, sortDirection, showHidden ->
		FilePickerState(sort, sortDirection, showHidden, currentFilter, currentDirectory)
	}.combine(_config) { ostate, cfg ->
		ostate.copy(config = cfg)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Lazily,
		initialValue = FilePickerState(
			FilePickerState.SortBy.NAME
		)
	)

	val pfactory = filePickerState.mapLatest{
		InvalidatingPagingSourceFactory({
			FilesystemPagingSource(it)
		})
	}


	val dirEntriesFlow = pfactory.flatMapLatest {
		Log.d("direntries", "refresh ${currentDirectory}")
		Pager(config = PagingConfig(pageSize = 50, enablePlaceholders = true), pagingSourceFactory = it).flow.cachedIn(viewModelScope)
	}


}