package sh.tablet.android.compose.filepicker.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import sh.tablet.android.compose.filepicker.FilePicker
import sh.tablet.android.compose.filepicker.FilePickerConfig
import java.nio.file.Path

@Composable
internal fun InnerFilePickerDialog(
	cfg: FilePickerConfig,
	onConfirmPick: (s: List<Path>) -> Unit,
	onDismissRequest: () -> Unit
) {
	Dialog(onDismissRequest = {
		onDismissRequest()
	}) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(0.8f),
			shape = RoundedCornerShape(16.dp)
		) {
			Column(
				modifier = Modifier.padding(12.dp),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				FilePicker(
					cfg, onFilesPicked = { it: List<Path> ->
					onConfirmPick(it)
					onDismissRequest()
				}, onCancel = onDismissRequest,
					confirmButton = { cnt, cb ->
						Button(onClick = cb, enabled = cnt > 0) {
							if (cfg.allowMultiple) {
								Text("Select ($cnt)")
							} else {
								Text("Select")
							}
						}
					}
				)
			}
		}
	}
}

@Composable
fun FilePickerDialog(
	cfg: FilePickerConfig,
	onConfirmPick: (s: List<Path>) -> Unit,
	onDismissRequest: () -> Unit
) {
	InnerFilePickerDialog(
		cfg,
		onConfirmPick = onConfirmPick,
		onDismissRequest = onDismissRequest
	)
}