package com.example.storyefun.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.storyefun.ui.components.BottomBar
import com.example.storyefun.ui.components.Header
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.ThemeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storyefun.viewModel.PostViewModel

@ExperimentalMaterial3Api
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeBookScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val theme = LocalAppColors.current // Access theme colors

    Scaffold(
        topBar = {
            Header(navController = navController, themeViewModel = themeViewModel)
        },
        bottomBar = { BottomBar(navController, "home", themeViewModel = themeViewModel) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background) // Use theme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { BookTabScreen(navController) }
            }
        }
    }
}

@Composable
fun BookTabScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Books", "Audio books")

    Column(modifier = Modifier) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            contentColor = Color.Black,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF00897B)
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) Color.Black else Color.Gray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTabIndex) {
            0 -> {
                NewBookScreen(navController)
                Spacer(modifier = Modifier.height(16.dp))
                RecommendedBookScreen(navController)
                Spacer(modifier = Modifier.height(16.dp))
                PopularBookScreen(navController)
                Spacer(modifier = Modifier.height(16.dp))
            }
            1 -> PostScreen()
        }
    }
}

//@Composable
//fun AudioBooksContent(navController: NavController) {
//    // Placeholder for AudioBooksContent
//    Text(
//        text = "Audio Books Content (To be implemented)",
//        color = LocalAppColors.current.textPrimary,
//        modifier = Modifier.padding(16.dp)
//    )
//}