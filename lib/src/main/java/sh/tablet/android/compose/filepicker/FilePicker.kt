@file:OptIn(ExperimentalCoroutinesApi::class)

package sh.tablet.android.compose.filepicker

import android.Manifest
import android.os.Environment
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sh.tablet.android.compose.filepicker.viewmodel.FilePickerViewModel
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.tablet.android.compose.filepicker.viewmodel.FilePickerState

@Composable
internal fun FilePickerDropdown(
	vm: FilePickerViewModel,
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	onSortClicked: () -> Unit
) {
	val showHiddenInteraction = remember {
		MutableInteractionSource()
	}
	val endPad = Modifier.padding(end = 16.dp)
	DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
		val showHidden = vm.showHiddenState.collectAsState()
		DropdownMenuItem(text = {
			Row(verticalAlignment = Alignment.CenterVertically, modifier = endPad) {
				Checkbox(checked = showHidden.value, onCheckedChange = {
					vm.setShowHidden(it)
					onDismissRequest()
				}, modifier = Modifier.padding(0.dp))
				Text("Show hidden items")
			}
		}, interactionSource = showHiddenInteraction, onClick = {
			vm.setShowHidden(!vm.showHiddenState.value)
			onDismissRequest()
		})
		DropdownMenuItem(text = {
			Row(verticalAlignment = Alignment.CenterVertically, modifier = endPad) {
				Icon(
					Icons.Default.Sort,
					contentDescription = "Sort by...",
					modifier = Modifier.padding(12.dp)
				)
				Text("Sort by...")
			}
		}, onClick = {
			onSortClicked()
			onDismissRequest()
		})
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilePickerSortSheet(
	sortBy: FilePickerState.SortBy,
	updateSortBy: (FilePickerState.SortBy) -> Unit,
	onDismissRequest: () -> Unit,
	sheetState: SheetState,
) {
	val scope = rememberCoroutineScope()
	var localSortBy by remember{mutableStateOf(sortBy)}
	LaunchedEffect(sortBy) {
		localSortBy = sortBy
	}
	ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
		Column {
			Column(Modifier.selectableGroup().padding(horizontal = 16.dp)) {
				FilePickerState.SortBy.entries.forEach {
					Row(
						Modifier
							.fillMaxWidth()
							.height(56.dp)
							.selectable(
								selected = localSortBy == it,
								onClick = {
									localSortBy = it
								},
								role = Role.RadioButton
							)
							.padding(horizontal = 16.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = (localSortBy == it),
							onClick = null
						)
						Text(text = it.name[0].uppercase() + it.name.slice(1 until it.name.length).lowercase(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
					}
				}
			}
		}
		Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
			OutlinedButton(onClick = onDismissRequest) {
				Text("Cancel")
			}
			Button(onClick = {
				scope.launch { sheetState.hide() }.invokeOnCompletion {
					if(!sheetState.isVisible) {
						updateSortBy(localSortBy)
						onDismissRequest()
					}
				}
			}) {
				Text("Save")
			}
		}
	}
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun InnerFilePicker(
	config: FilePickerConfig,
	directoryIcon: @Composable (Modifier) -> Unit,
	fileIcon: @Composable (Modifier) -> Unit,
	confirmButton: @Composable (Int, () -> Unit) -> Unit,
	cancelButton: @Composable (() -> Unit) -> Unit,
	onFilesPicked: (files: List<Path>) -> Unit,
	onCancel: () -> Unit,
) {
	val storagePermissionState =
		rememberPermissionState(permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE)
	val vm: FilePickerViewModel = viewModel()
	val cfg = remember {
		config
	}
	val listState = rememberSaveable(saver = LazyListState.Saver) {
		LazyListState()
	}
	val lazyDirEntries = vm.dirEntriesFlow.collectAsLazyPagingItems()
	val curDirState by vm.currentDirectoryState.collectAsState()

	val backEnabled by remember {
		derivedStateOf() {
			curDirState.absolutePathString() != Environment.getExternalStorageDirectory().absolutePath
		}
	}

	var menuShown by remember { mutableStateOf(false) }
	var sortSheetShown by remember { mutableStateOf(false) }

	BackHandler(
		enabled = backEnabled
	) {
		vm.currentDirectory = vm.currentDirectory?.parent
	}
	LaunchedEffect(storagePermissionState.status, cfg) {
		if (!storagePermissionState.status.isGranted) {
			Log.d(BuildConfig.LIBRARY_PACKAGE_NAME, "launching!")
			storagePermissionState.launchPermissionRequest()
		}
		vm.config = cfg
	}
	LaunchedEffect(cfg.initialDirectory) {
		vm.pfactory.collect {
			it.invalidate()
		}
		vm.currentDirectory = Paths.get(cfg.initialDirectory)
	}


	val selectedEntries = rememberSaveable {
		mutableStateSetOf<String>()
	}

	val currentFilter = vm.filterState.collectAsState()
	val sortBy = vm.sortState.collectAsState()
	val sortDirection = vm.sortDirectionState.collectAsState()

	val sortSheetState = rememberModalBottomSheetState()

	val onSelectedChange: (Boolean, Path) -> Unit = { sel, p ->
		if (!config.allowMultiple)
			selectedEntries.clear()
		if (sel)
			selectedEntries.add(p.toAbsolutePath().absolutePathString())
		else if (config.allowMultiple)
			selectedEntries.remove(p.toAbsolutePath().absolutePathString())
	}
	val onDirClick: (Path) -> Unit = { p ->
		vm.currentDirectory = p
		runBlocking {
			listState.scrollToItem(0)
		}
	}

	Column(
		modifier = Modifier
			.fillMaxHeight()
			.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			OutlinedTextField(
				modifier = Modifier.weight(1f),
				value = currentFilter.value,
				onValueChange = vm::setFilter,
				leadingIcon = {
					Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
				},
				singleLine = true
			)
			IconButton(onClick = {
				vm.setSortDirection(sortDirection.value.opposite())
			}) {
				Icon(
					if (sortDirection.value == FilePickerState.SortDirection.DESCENDING) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
					contentDescription = "Sort ${sortDirection.value.opposite().toString().lowercase()}"
				)
			}
			Box {
				IconButton(onClick = { menuShown = !menuShown }) {
					Icon(Icons.Default.MoreVert, contentDescription = "Options")
				}
				FilePickerDropdown(vm, menuShown, {
					menuShown = false
				}) {
					sortSheetShown = true
				}
			}
		}


		LazyColumn(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth(),
			state = listState
		) {
			item(key = "..") {
				FileItem(
					path = vm.currentDirectory ?: Paths.get(config.initialDirectory),
					parentConfig = vm.config,
					isSelected = false,
					fileIcon = {},
					onSelectedChange = { _, _ ->

					},
					directoryIcon = directoryIcon,
					onDirectoryClick = onDirClick,
					isGoToParent = true
				)
			}
			items(
				lazyDirEntries.itemCount,
				key = { oit ->
					val kf = lazyDirEntries.itemKey {
						Log.d("itemkey", it.toAbsolutePath().absolutePathString())
						it.toAbsolutePath().hashCode()
					}
					kf(oit)
				}
			)
			{ idx ->
				val item = lazyDirEntries[idx]
				if (item != null) {
					FileItem(
						path = item,
						parentConfig = vm.config,
						isSelected = selectedEntries.contains(item.toAbsolutePath().absolutePathString()),
						fileIcon = fileIcon,
						directoryIcon = directoryIcon,
						onSelectedChange = onSelectedChange,
						onDirectoryClick = onDirClick
					)
				} else {
					Box(
						Modifier
							.fillMaxWidth()
							.padding(2.dp)
							.height(24.dp),
						contentAlignment = Alignment.CenterStart
					) {
						Text(text = "...")
					}
				}
			}
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(6.dp, alignment = Alignment.End)
		) {
			cancelButton(onCancel)
			confirmButton(selectedEntries.size) {
				onFilesPicked(selectedEntries.toList().map {
					Paths.get(it)
				})
			}
		}
	}
	if (sortSheetShown) {
		FilePickerSortSheet(sortBy.value, vm::setSort, onDismissRequest = {
			sortSheetShown = false
		}, sortSheetState)
	}
}

@Composable
@JvmName("MultiFilePicker")
fun FilePicker(
	config: FilePickerConfig,
	onFilesPicked: (files: List<Path>) -> Unit = {},
	onCancel: () -> Unit = {},
	directoryIcon: @Composable (Modifier) -> Unit = DefaultDirectoryIcon,
	fileIcon: @Composable (Modifier) -> Unit = DefaultFileIcon,
	confirmButton: @Composable (Int, () -> Unit) -> Unit = DefaultConfirmButton,
	cancelButton: @Composable (() -> Unit) -> Unit = DefaultCancelButton
) {
	InnerFilePicker(
		config,
		directoryIcon,
		fileIcon,
		onCancel = onCancel,
		cancelButton = cancelButton,
		confirmButton = confirmButton, onFilesPicked = {
			onFilesPicked(it)
		})
}

@Composable
fun FilePicker(
	config: FilePickerConfig,
	onFilePicked: (file: Path?) -> Unit = {},
	onCancel: () -> Unit = {},
	directoryIcon: @Composable (Modifier) -> Unit = DefaultDirectoryIcon,
	fileIcon: @Composable (Modifier) -> Unit = DefaultFileIcon,
	confirmButton: @Composable (Int, () -> Unit) -> Unit = DefaultConfirmButton,
	cancelButton: @Composable (() -> Unit) -> Unit = DefaultCancelButton
) {
	InnerFilePicker(
		config,
		directoryIcon,
		fileIcon,
		onCancel = onCancel,
		cancelButton = cancelButton,
		confirmButton = confirmButton, onFilesPicked = {
			onFilePicked(it.firstOrNull())
		})
}