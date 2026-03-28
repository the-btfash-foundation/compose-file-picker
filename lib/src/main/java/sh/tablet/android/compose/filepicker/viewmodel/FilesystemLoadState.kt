package sh.tablet.android.compose.filepicker.viewmodel

sealed interface FilesystemLoadState {
	data class Success<T>(val data: T) : FilesystemLoadState
	data class Error(val err: Throwable) : FilesystemLoadState
	object Loading : FilesystemLoadState
}