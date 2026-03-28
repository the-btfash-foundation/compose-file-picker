package sh.tablet.android.compose.filepicker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal val DefaultFileIcon: @Composable (Modifier) -> Unit = {
	Icon(
		modifier = it,
		imageVector = Icons.Outlined.InsertDriveFile,
		contentDescription = "Folder",
		tint = MaterialTheme.colorScheme.onPrimary
	)
}

internal val DefaultDirectoryIcon: @Composable (Modifier) -> Unit = {
	Icon(
		modifier = it,
		imageVector = Icons.Default.Folder,
		contentDescription = "Folder",
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	)
}

internal val DefaultConfirmButton: @Composable (Int, () -> Unit) -> Unit = { numSelected, cb ->
	Button(onClick = cb, enabled = numSelected > 0) {
		Text("Select (${numSelected})")
	}
}

internal val DefaultCancelButton: @Composable (() -> Unit) -> Unit = {
	OutlinedButton(onClick = it, border = ButtonDefaults.outlinedButtonBorder(true).copy(width = 1.5.dp)) {
		Text("Cancel")
	}
}