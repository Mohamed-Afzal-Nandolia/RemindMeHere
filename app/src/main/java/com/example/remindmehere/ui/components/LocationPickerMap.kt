package com.example.remindmehere.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.remindmehere.theme.*
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun LocationPickerMap(
    initialLat: Double? = null,
    initialLng: Double? = null,
    radiusMeters: Float = 200f,
    onLocationPicked: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pickedPoint by remember { mutableStateOf(initialLat?.let { GeoPoint(it, initialLng!!) }) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(CARTO_DARK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(pickedPoint ?: GeoPoint(20.5937, 78.9629)) // Default: India

                    // My location overlay
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)

                    // Tap listener
                    overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                        override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                            val projection = mapView.projection
                            val point = projection.fromPixels(e.x.toInt(), e.y.toInt())
                            val geo = GeoPoint(point.latitude, point.longitude)
                            pickedPoint = geo
                            onLocationPicked(geo.latitude, geo.longitude)
                            updateOverlays(mapView, geo, radiusMeters)
                            return true
                        }
                    })

                    pickedPoint?.let { updateOverlays(this, it, radiusMeters) }
                    mapViewRef = this
                }
            },
            update = { mapView ->
                pickedPoint?.let { updateOverlays(mapView, it, radiusMeters) }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Center on my location button
        FloatingActionButton(
            onClick = {
                mapViewRef?.overlays
                    ?.filterIsInstance<MyLocationNewOverlay>()
                    ?.firstOrNull()?.myLocation?.let { loc ->
                        mapViewRef?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                    }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(42.dp),
            containerColor = VioletPrimary,
            contentColor = OnPrimary
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = "My location", modifier = Modifier.size(20.dp))
        }
    }
}

private fun updateOverlays(mapView: MapView, center: GeoPoint, radiusMeters: Float) {
    mapView.overlays.removeAll { it is Marker || it is Polygon }

    // Pin marker
    val marker = Marker(mapView).apply {
        position = center
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Reminder location"
    }
    mapView.overlays.add(marker)

    // Radius circle
    val circle = buildCircle(center, radiusMeters.toDouble())
    mapView.overlays.add(circle)
    mapView.invalidate()
}

private fun buildCircle(center: GeoPoint, radiusMeters: Double): Polygon {
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
        fillPaint.color = 0x220AB5CC   // Cyan fill, semi-transparent
        outlinePaint.color = 0xFF06B6D4.toInt()
        outlinePaint.strokeWidth = 2f
    }
}

/** CartoDB Dark Matter — free, no API key, dark-theme friendly */
val CARTO_DARK = XYTileSource(
    "CartoDB.DarkMatter",
    0, 19, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/dark_all/",
        "https://b.basemaps.cartocdn.com/dark_all/",
        "https://c.basemaps.cartocdn.com/dark_all/",
        "https://d.basemaps.cartocdn.com/dark_all/"
    )
)
