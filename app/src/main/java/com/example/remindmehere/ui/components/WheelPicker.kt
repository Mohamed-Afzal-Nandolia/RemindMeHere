package com.example.remindmehere.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 48.dp,
    visibleItemsCount: Int = 5,
    initialIndex: Int = 0,
    onItemSelected: (index: Int, item: T) -> Unit,
    content: @Composable (item: T) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val halfVisibleCount = visibleItemsCount / 2

    // Pad items so first and last can be centered
    val paddedItems = remember(items) {
        val pad = List(halfVisibleCount) { null }
        pad + items + pad
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { it }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    onItemSelected(index, items[index])
                }
            }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(paddedItems.size) { i ->
                val item = paddedItems[i]
                
                // Calculate item offset for 3D effect
                val indexOffset = remember {
                    derivedStateOf {
                        val firstVisible = listState.firstVisibleItemIndex
                        val offset = i - (firstVisible + halfVisibleCount)
                        offset
                    }
                }
                
                val offset = indexOffset.value
                val distance = offset.absoluteValue.toFloat()
                
                val rotationX = (offset * 25f).coerceIn(-90f, 90f)
                val alpha = (1f - (distance * 0.4f)).coerceAtLeast(0.2f)
                val scale = (1f - (distance * 0.15f)).coerceAtLeast(0.7f)

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .graphicsLayer {
                            this.rotationX = rotationX
                            this.alpha = alpha
                            this.scaleX = scale
                            this.scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (item != null) {
                        content(item)
                    }
                }
            }
        }
    }
}

@Composable
fun WheelDateTimePicker(
    initialTime: Long,
    onTimeChanged: (Long) -> Unit
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialTime }) }
    
    val days = (1..31).toList()
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val years = (calendar.get(Calendar.YEAR)..(calendar.get(Calendar.YEAR) + 10)).toList()
    
    val hours = (1..12).toList()
    val minutes = (0..59).toList()
    val amPm = listOf("AM", "PM")

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date Wheels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WheelPicker<Int>(
                items = days,
                initialIndex = calendar.get(Calendar.DAY_OF_MONTH) - 1,
                modifier = Modifier.weight(1f),
                onItemSelected = { _, day ->
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    onTimeChanged(calendar.timeInMillis)
                }
            ) { Text(it.toString(), fontSize = 18.sp, color = Color.White) }
            
            WheelPicker<String>(
                items = months,
                initialIndex = calendar.get(Calendar.MONTH),
                modifier = Modifier.weight(1f),
                onItemSelected = { idx, _ ->
                    calendar.set(Calendar.MONTH, idx)
                    onTimeChanged(calendar.timeInMillis)
                }
            ) { Text(it, fontSize = 18.sp, color = Color.White) }
            
            WheelPicker<Int>(
                items = years,
                initialIndex = 0,
                modifier = Modifier.weight(1f),
                onItemSelected = { _, year ->
                    calendar.set(Calendar.YEAR, year)
                    onTimeChanged(calendar.timeInMillis)
                }
            ) { Text(it.toString(), fontSize = 18.sp, color = Color.White) }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Time Wheels
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val currentHour = calendar.get(Calendar.HOUR)
            WheelPicker<Int>(
                items = hours,
                initialIndex = if (currentHour == 0) 11 else currentHour - 1,
                modifier = Modifier.weight(1f),
                onItemSelected = { _, hour ->
                    calendar.set(Calendar.HOUR, if (hour == 12) 0 else hour)
                    onTimeChanged(calendar.timeInMillis)
                }
            ) { Text(it.toString().padStart(2, '0'), fontSize = 18.sp, color = Color.White) }
            
            Text(":", fontSize = 24.sp, color = Color.White, modifier = Modifier.align(Alignment.CenterVertically))
            
            WheelPicker<Int>(
                items = minutes,
                initialIndex = calendar.get(Calendar.MINUTE),
                modifier = Modifier.weight(1f),
                onItemSelected = { _, min ->
                    calendar.set(Calendar.MINUTE, min)
                    onTimeChanged(calendar.timeInMillis)
                }
            ) { Text(it.toString().padStart(2, '0'), fontSize = 18.sp, color = Color.White) }
            
            WheelPicker<String>(
                items = amPm,
                initialIndex = calendar.get(Calendar.AM_PM),
                modifier = Modifier.weight(1f),
                onItemSelected = { idx, _ ->
                    calendar.set(Calendar.AM_PM, idx)
                    onTimeChanged(calendar.timeInMillis)
                }
            ) { Text(it, fontSize = 18.sp, color = Color.White) }
        }
    }
}

