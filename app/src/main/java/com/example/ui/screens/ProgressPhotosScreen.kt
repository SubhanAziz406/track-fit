package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProgressPhoto
import com.example.ui.FitTrackViewModel
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun ProgressPhotosScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val photos by viewModel.allProgressPhotos.collectAsState()
    val context = LocalContext.current
    
    var beforePhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var afterPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var showSliderDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val base64 = bitmapToBase64(bitmap)
                    viewModel.addProgressPhoto(base64, "Progress Update")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Visual Timeline",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Compare your body evolution over time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Button(
                onClick = { photoPickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("upload_progress_photo")
            ) {
                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Photo", fontWeight = FontWeight.Bold)
            }
        }

        // Draggable before/after trigger button
        if (photos.size >= 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    beforePhoto = photos.lastOrNull() // Oldest
                    afterPhoto = photos.firstOrNull() // Newest
                    showSliderDialog = true
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Compare, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Column {
                            Text("Before/After Slider Compare", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Draggable visual timeline comparisons", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Photos Grid List
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                    Text(
                        text = "No progress photos uploaded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(photos) { photo ->
                    PhotoCard(
                        photo = photo,
                        onDelete = { viewModel.deleteProgressPhoto(photo.id) },
                        isSelectedForBefore = photo == beforePhoto,
                        isSelectedForAfter = photo == afterPhoto,
                        onSelectBefore = { beforePhoto = photo },
                        onSelectAfter = { afterPhoto = photo }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showSliderDialog && beforePhoto != null && afterPhoto != null) {
        BeforeAfterSliderDialog(
            before = beforePhoto!!,
            after = afterPhoto!!,
            onDismiss = { showSliderDialog = false }
        )
    }
}

@Composable
fun PhotoCard(
    photo: ProgressPhoto,
    onDelete: () -> Unit,
    isSelectedForBefore: Boolean,
    isSelectedForAfter: Boolean,
    onSelectBefore: () -> Unit,
    onSelectAfter: () -> Unit
) {
    val bitmap = remember(photo.imageBase64) {
        base64ToBitmap(photo.imageBase64)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Progress Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.BrokenImage, contentDescription = null)
                    }
                }

                // Delete Button Overlay
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                }

                // Date stamp
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = photo.dateString, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Slider selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onSelectBefore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelectedForBefore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelectedForBefore) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Before", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSelectAfter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelectedForAfter) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelectedForAfter) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("After", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BeforeAfterSliderDialog(
    before: ProgressPhoto,
    after: ProgressPhoto,
    onDismiss: () -> Unit
) {
    val bitmapBefore = remember(before) { base64ToBitmap(before.imageBase64) }
    val bitmapAfter = remember(after) { base64ToBitmap(after.imageBase64) }
    
    var sliderXFraction by remember { mutableStateOf(0.5f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Before/After Drag Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Interactive slider canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                val pixelsWide = size.width
                                val newFraction = sliderXFraction + (dragAmount / pixelsWide)
                                sliderXFraction = newFraction.coerceIn(0.02f, 0.98f)
                            }
                        }
                ) {
                    // Underlay: After Photo (Modern body)
                    if (bitmapAfter != null) {
                        Image(
                            bitmap = bitmapAfter.asImageBitmap(),
                            contentDescription = "After",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Overlay: Before Photo (Clips using sliderXFraction)
                    if (bitmapBefore != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(sliderXFraction)
                                .clip(RoundedCornerShape(0.dp))
                        ) {
                            Image(
                                bitmap = bitmapBefore.asImageBitmap(),
                                contentDescription = "Before",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    // Make sure width doesn't stretch, overlay matches alignment
                                    .requiredWidth(350.dp),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterStart
                            )
                        }
                    }

                    // Vertically sliding drag-divider
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(4.dp)
                            .align(Alignment.CenterStart)
                            .graphicsLayer(translationX = sliderXFraction * 280f) // Mock scale mapping
                            .background(Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White, CircleShape)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Drag Me",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Label indicators
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Before", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "After", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Done Comparing", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper Encoders / Decoders
fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
