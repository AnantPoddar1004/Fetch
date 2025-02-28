package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.tooling.preview.Preview


// Define Colors
val DarkPurple = Color(0xFF300D38)
val LightBackground = Color(0xFFF9F8F9)
val AccentColor = Color(0xFFFFA900)

// Load Fonts
val SyneFont = FontFamily(Font(R.font.syne_regular, FontWeight.Normal))
val LexendFont = FontFamily(Font(R.font.lexend_regular, FontWeight.Normal))

// Data Model
data class Item(val id: Int, val listId: Int, val name: String?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JsonListDisplayApp()
        }
    }
}

@Composable
fun JsonListDisplayApp() {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var expandedLists by remember { mutableStateOf(setOf<Int>()) }
    val allCollapsed = expandedLists.isEmpty()

    LaunchedEffect(Unit) {
        items = fetchJsonData("https://fetch-hiring.s3.amazonaws.com/hiring.json")
    }

    val filteredSortedItems = items
        .filter { it.name != null && it.name.isNotBlank() }
        .sortedWith(compareBy({ it.listId }, { it.id }))
        .groupBy { it.listId }

    val allExpanded = expandedLists.size == filteredSortedItems.keys.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPurple)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp, top = 24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fetch_logo),
                contentDescription = "Fetch Logo",
                modifier = Modifier.size(34.sp.value.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Item List",
                fontFamily = LexendFont,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AccentColor
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = { expandedLists = filteredSortedItems.keys.toSet() },
                colors = ButtonDefaults.buttonColors(containerColor = if (allExpanded) LightBackground else AccentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text("Expand All", fontFamily = LexendFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkPurple)
            }
            Button(
                onClick = { expandedLists = emptySet() },
                colors = ButtonDefaults.buttonColors(containerColor = if (allCollapsed) LightBackground else AccentColor),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Collapse All", fontFamily = LexendFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkPurple)
            }
        }

        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            filteredSortedItems.forEach { (listId, items) ->
                item {
                    ListHeader(
                        listId = listId,
                        isExpanded = expandedLists.contains(listId),
                        onToggle = {
                            expandedLists = if (expandedLists.contains(listId)) {
                                expandedLists - listId
                            } else {
                                expandedLists + listId
                            }
                        }
                    )
                }
                if (expandedLists.contains(listId)) {
                    items(items) { item ->
                        ListItem(text = item.name ?: "Unknown")
                    }
                }
            }
        }
    }
}

@Composable
fun ListHeader(listId: Int, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onToggle() },
        border = BorderStroke(2.dp, AccentColor),
        colors = CardDefaults.cardColors(containerColor = DarkPurple)
    ) {
        Text(
            text = if (isExpanded) "▼ List ID: $listId" else "▶ List ID: $listId",
            fontFamily = LexendFont,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LightBackground,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ListItem(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(start = 24.dp),
        colors = CardDefaults.cardColors(containerColor = LightBackground)
    ) {
        Text(
            text = text,
            fontFamily = LexendFont,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = DarkPurple,
            modifier = Modifier.padding(8.dp)
        )
    }
}

suspend fun fetchJsonData(url: String): List<Item> {
    return withContext(Dispatchers.IO) {
        try {
            val jsonString = URL(url).readText()
            val itemType = object : TypeToken<List<Item>>() {}.type
            Gson().fromJson(jsonString, itemType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JsonListDisplayPreview() {
    JsonListDisplayApp()
}