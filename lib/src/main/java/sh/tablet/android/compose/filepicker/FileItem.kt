package sh.tablet.android.compose.filepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.TimeZone
import kotlin.io.path.absolutePathString
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Composable
fun FileThumbnail(path: Path, modifier: Modifier = Modifier) {
	val context = LocalContext.current
	val imageLoader = remember(context) {
		ImageLoader.Builder(context).components {
			add(VideoFrameDecoder.Factory())
		}.build()
	}

	AsyncImage(
		model = path.absolutePathString(),
		imageLoader = imageLoader,
		contentDescription = path.name,
		modifier = modifier,
		contentScale = ContentScale.Crop,
		alignment = Alignment.Center
	)
}

@Composable
fun FileItem(
	path: Path,
	parentConfig: FilePickerConfig,
	isSelected: Boolean,
	onSelectedChange: (Boolean, Path) -> Unit,
	onDirectoryClick: (Path) -> Unit,
	directoryIcon: @Composable (Modifier) -> Unit,
	fileIcon: @Composable (Modifier) -> Unit,
	isGoToParent: Boolean = false
) {
	val interactionSource = remember {
		MutableInteractionSource()
	}
	val dateModified = LocalDateTime.ofInstant(
		Instant.fromEpochMilliseconds(path.toFile().lastModified()).toJavaInstant(),
		TimeZone.getDefault().toZoneId()
	)
	val showCheckbox = if (parentConfig.onlyDirectories) {
		true
	} else {
		!path.isDirectory() && !isGoToParent
	}
	val rippler = ripple(true)
	Row(
		modifier = Modifier
			.clickable(
				interactionSource = if (!parentConfig.onlyDirectories) interactionSource else null,
				indication = rippler
			) {
				if (!isGoToParent) {
					if (path.isDirectory()) {
						onDirectoryClick(path)
					} else {
						onSelectedChange(!isSelected, path)
					}
				} else {
					onDirectoryClick(path.parent)
				}
			}
			.padding(5.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		val bgColor = if (path.isDirectory() || isGoToParent) {
			MaterialTheme.colorScheme.surfaceVariant
		} else {
			MaterialTheme.colorScheme.onPrimaryContainer
		}
		val size = if (path.isDirectory()) {
			"(${path.listDirectoryEntries().size})"
		} else {
			readableFileSize(path.fileSize())
		}
		val shape = RoundedCornerShape(9.dp)
		Box(
			modifier = Modifier
				.size(40.dp)
				.background(bgColor, shape = shape)
				.clip(shape)
				.padding(if (parentConfig.showThumbnails) Dp.Unspecified else 6.dp),
			contentAlignment = Alignment.Center
		) {
			if (path.isDirectory() || isGoToParent) {
				directoryIcon(Modifier.size(28.dp))
			} else {
				if (parentConfig.showThumbnails && isPreviewableMedia(path.toFile())) {
					FileThumbnail(
						path, Modifier
							.size(40.dp)
					)
				} else {
					fileIcon(Modifier.size(28.dp))
				}
			}
		}
		if (isGoToParent) {
			Text(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth(), text = ".."
			)
		} else {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					Text(text = path.fileName.name, lineHeight = 10.sp)
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							fontSize = 8.sp,
							text = "${parentConfig.getMTimeFormatter().format(dateModified)} | $size"
						)
					}
				}
				if (showCheckbox) {
					Checkbox(checked = isSelected, interactionSource = interactionSource, onCheckedChange = {
						onSelectedChange(it, path)
					})
				}
			}
		}
	}
}