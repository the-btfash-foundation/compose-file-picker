package sh.tablet.android.compose.filepicker.example.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.tablet.android.compose.filepicker.BuildConfig
import sh.tablet.android.compose.filepicker.FilePickerConfig

@Composable
fun SettingToggle(
	name: String,
	value: Boolean = false,
	label: String,
	toggleSetting: (String, Boolean) -> Unit
) {
	val interactionSource = remember {
		MutableInteractionSource()
	}
	Row(
		horizontalArrangement = Arrangement.spacedBy(2.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.clickable(onClick = {
				toggleSetting(name, !value)
			}, interactionSource = interactionSource)
			.padding(5.dp).fillMaxWidth()
	) {
		Checkbox(checked = value, onCheckedChange = {
			toggleSetting(name, it)
		}, interactionSource = interactionSource)
		Text(text = label)
	}
}

internal val SETTING_ONLY_DIRECTORIES = "only-directories"
internal val SETTING_SHOW_THUMBNAILS = "show-thumbnails"
internal val SETTING_ALLOW_MULITPLE = "allow-multiple"

@Composable
fun FilePickerSettings(onSettingsChanged: (FilePickerConfig) -> Unit) {
	var pattern by rememberSaveable { mutableStateOf("*") }
	var onlyDirectories by rememberSaveable { mutableStateOf(false) }
	var showThumbnails by rememberSaveable { mutableStateOf(false) }
	var allowMultiple by rememberSaveable { mutableStateOf(false) }

	val cfg by remember {
		derivedStateOf {
			FilePickerConfig(pattern, onlyDirectories, showThumbnails, allowMultiple = allowMultiple)
		}
	}
	val toggler: (String, Boolean) -> Unit = { n, v ->
		when (n) {
			SETTING_ONLY_DIRECTORIES -> {
				onlyDirectories = v
			}

			SETTING_SHOW_THUMBNAILS -> {
				showThumbnails = v
			}

			SETTING_ALLOW_MULITPLE -> {
				allowMultiple = v
			}
		}
	}

	LaunchedEffect(cfg) {
		Log.d(BuildConfig.LIBRARY_PACKAGE_NAME, cfg.initialDirectory)
		onSettingsChanged(cfg)
	}

	Column(
		modifier = Modifier
			.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Column(
			modifier = Modifier.fillMaxWidth(0.75f),
		) {
			Column(
				modifier = Modifier
					.padding(6.dp).fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
				horizontalAlignment = Alignment.Start
			) {
				SettingToggle(SETTING_ONLY_DIRECTORIES, onlyDirectories, "Directory-only mode", toggler)
				SettingToggle(SETTING_SHOW_THUMBNAILS, showThumbnails, "Show media thumbnails", toggler)
				SettingToggle(SETTING_ALLOW_MULITPLE, allowMultiple, "Allow multi-select", toggler)
			}
			OutlinedTextField(
				modifier = Modifier.fillMaxWidth(),
				value = pattern,
				singleLine = true,
				onValueChange = {
					pattern = it
				},
				label = {
					Text("Glob pattern of files to show")
				})
		}
	}
}
