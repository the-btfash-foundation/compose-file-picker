package sh.tablet.android.compose.filepicker.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import sh.tablet.android.compose.filepicker.FilePicker
import sh.tablet.android.compose.filepicker.FilePickerConfig
import sh.tablet.android.compose.filepicker.example.ui.components.FilePickerDialog
import sh.tablet.android.compose.filepicker.example.ui.components.FilePickerSettings
import sh.tablet.android.compose.filepicker.example.ui.theme.ComposeFilePickerTheme
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			ComposeFilePickerTheme {
				Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					Box(modifier = Modifier.padding(innerPadding)) {
						MainComponent(Modifier.padding(20.dp))
					}
				}
			}
		}
	}
}

@Composable
fun ResultDialog(files: List<Path>, onDismissRequest: () -> Unit) {
	Dialog(onDismissRequest = onDismissRequest) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					text = "You selected the following files",
					fontWeight = FontWeight.Bold,
					fontSize = 25.sp,
					textAlign = TextAlign.Center
				)
				Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
					files.forEach {
						Text(text =  "\u2606 ${it.absolutePathString()}", fontFamily = FontFamily.Monospace)
					}
				}
			}
		}
	}
}

@Composable
fun MainComponent(modifier: Modifier) {
	val openPickerDialog = remember { mutableStateOf(false) }
	var filePickerConfig by remember {
		mutableStateOf(FilePickerConfig())
	}
	var openResultDialog by remember {
		mutableStateOf(false)
	}
	val selectedFiles = remember {
		mutableStateListOf<Path>()
	}
	Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
		FilePickerSettings {
			filePickerConfig = it
		}
		Button(onClick = {
			openPickerDialog.value = true
		}, modifier = Modifier.fillMaxWidth(0.8f)) {
			Text(text = "Open file picker")
		}
	}
	when {
		openPickerDialog.value -> FilePickerDialog(
			filePickerConfig,
			onConfirmPick = { it: List<Path> ->
				selectedFiles.clear()
				selectedFiles.addAll(it)
				openResultDialog = true
			},
			onDismissRequest = {
				openPickerDialog.value = false
			})

		openResultDialog -> ResultDialog(selectedFiles) {
			openResultDialog = false
		}
	}
}


@Preview(showBackground = true)
@Composable
fun FilePickerPreview() {
	ComposeFilePickerTheme {
		FilePicker(config = FilePickerConfig(), onFilePicked = {})
	}
}