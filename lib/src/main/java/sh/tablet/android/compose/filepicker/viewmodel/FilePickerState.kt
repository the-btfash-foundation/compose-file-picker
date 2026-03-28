package sh.tablet.android.compose.filepicker.viewmodel

import android.os.Environment
import sh.tablet.android.compose.filepicker.FilePickerConfig
import java.nio.file.Path
import kotlin.io.path.absolute

data class FilePickerState(
	val sortBy: SortBy,
	val sortDirection: SortDirection = SortDirection.ASCENDING,
	val showHidden: Boolean = true,
	val currentFilter: String = "",
	val currentDirectory: Path = Environment.getExternalStorageDirectory().toPath().absolute(),
	val config: FilePickerConfig = FilePickerConfig()
) {
	enum class SortBy {
		NAME,
		DATE,
		SIZE,
		TYPE
	}

	enum class SortDirection {
		ASCENDING,
		DESCENDING;
		fun opposite(): SortDirection {
			return if(this == SortDirection.ASCENDING) {
				DESCENDING
			} else {
				ASCENDING
			}
		}
	}

}
