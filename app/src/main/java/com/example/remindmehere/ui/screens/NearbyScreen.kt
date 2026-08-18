package com.example.remindmehere.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remindmehere.data.model.ReminderStatus
import com.example.remindmehere.theme.*
import com.example.remindmehere.ui.components.CARTO_DARK
import com.example.remindmehere.ui.components.ReminderCard
import com.example.remindmehere.ui.viewmodel.DashboardViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun NearbyScreen(vm: DashboardViewModel = hiltViewModel(), onNavigateToHistory: () -> Unit) {
    val locationReminders by vm.locationReminders.collectAsStateWithLifecycle()
    val active = locationReminders.filter { it.status == ReminderStatus.PENDING }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(CyanAccent.copy(0.25f), DeepNavy)))
                    .padding(top = 56.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Radar, null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nearby", style = MaterialTheme.typography.labelLarge, color = CyanAccent)
                        Spacer(Modifier.width(8.dp))
                        if (active.isNotEmpty()) {
                            // Pulsing live indicator
                            Surface(color = CyanAccent.copy(0.15f), shape = RoundedCornerShape(20.dp)) {
                                Text(
                                    "● LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Outlined.History, contentDescription = "History", tint = CyanAccent)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Location reminders",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
            }

            // Map
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(CARTO_DARK)
                        setMultiTouchControls(true)
                        controller.setZoom(13.0)

                        val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        locOverlay.enableMyLocation()
                        locOverlay.enableFollowLocation()
                        overlays.add(locOverlay)

                        active.forEach { reminder ->
                            val lat = reminder.latitude ?: return@forEach
                            val lng = reminder.longitude ?: return@forEach
                            val geo = GeoPoint(lat, lng)

                            val marker = Marker(this).apply {
                                position = geo
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = reminder.title
                                snippet = reminder.placeName.ifBlank { "%.4f, %.4f".format(lat, lng) }
                            }
                            overlays.add(marker)
                            overlays.add(buildNearbyCircle(geo, reminder.radiusMeters.toDouble()))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Bottom list
            if (locationReminders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No location reminders", color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${active.size} active",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(locationReminders, key = { it.id }) { reminder ->
                        Box(modifier = Modifier.animateItem()) {
                            ReminderCard(reminder, onMarkDone = { vm.markDone(it) }, onDelete = { vm.deleteReminder(it) })
                        }
                    }
                }
            }
        }
    }
}

private fun buildNearbyCircle(center: GeoPoint, radiusMeters: Double): Polygon {
    val points = mutableListOf<GeoPoint>()
    val earthRadius = 6_371_000.0
    val lat = Math.toRadians(center.latitude)
    val lng = Math.toRadians(center.longitude)
    val d = radiusMeters / earthRadius
    for (i in 0..360 step 5) {
        val bearing = Math.toRadians(i.toDouble())
        val pLat = Math.asin(Math.sin(lat) * Math.cos(d) + Math.cos(lat) * Math.sin(d) * Math.cos(bearing))
        val pLng = lng + Math.atan2(
            Math.sin(bearing) * Math.sin(d) * Math.cos(lat),
            Math.cos(d) - Math.sin(lat) * Math.sin(pLat)
        )
        points.add(GeoPoint(Math.toDegrees(pLat), Math.toDegrees(pLng)))
    }
    return Polygon().apply {
        this.points = points
        fillPaint.color = 0x220AB5CC
        outlinePaint.color = 0xFF06B6D4.toInt()
        outlinePaint.strokeWidth = 2f
    }
}
