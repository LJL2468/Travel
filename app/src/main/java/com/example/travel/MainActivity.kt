package com.example.travel

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travel.ui.theme.TravelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "attractions_list") {
        composable("attractions_list") {
            TouristAttractionsList(navController)
        }
        composable(
            route = "attraction_details/{attractionName}/{shortDescription}/{longDescription}/{attractionImageResId}/{mapUrl}",
            arguments = listOf(
                navArgument("attractionName") { type = NavType.StringType },
                navArgument("shortDescription") { type = NavType.StringType },
                navArgument("longDescription") { type = NavType.StringType },
                navArgument("attractionImageResId") { type = NavType.IntType },
                navArgument("mapUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val attractionName = backStackEntry.arguments?.getString("attractionName")?.decode() ?: ""
            val shortDescription = backStackEntry.arguments?.getString("shortDescription")?.decode() ?: ""
            val longDescription = backStackEntry.arguments?.getString("longDescription")?.decode() ?: ""
            val attractionImageResId = backStackEntry.arguments?.getInt("attractionImageResId") ?: 0
            val mapUrl = backStackEntry.arguments?.getString("mapUrl")?.decode() ?: ""

            TouristAttractionDetails(
                name = attractionName,
                shortDescription = shortDescription,
                longDescription = longDescription,
                imageResId = attractionImageResId,
                mapUrl = mapUrl,
                navController = navController
            )
        }
    }
}

fun String.encode(): String = Uri.encode(this)

fun String.decode(): String = Uri.decode(this)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouristAttractionsList(navController: NavHostController) {
    val attractions = remember { AttractionsRepository.getAttractions() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Tourist Attractions") }
            )
        }
    ) {
        LazyColumn {
            items(attractions) { attraction ->
                TouristAttractionItem(attraction = attraction) {
                    navController.navigate(
                        "attraction_details/${attraction.name.encode()}/${attraction.shortDescription.encode()}/${attraction.longDescription.encode()}/${attraction.imageResId}/${attraction.mapUrl.encode()}"
                    )
                }
            }
        }
    }
}

@Composable
fun TouristAttractionItem(attraction: TouristAttraction, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = attraction.imageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(text = attraction.name, fontSize = 20.sp)
                Text(text = attraction.shortDescription, fontSize = 16.sp, color = Color.Gray)
            }
        }
    }
}

data class TouristAttraction(
    val name: String,
    val shortDescription: String,
    val longDescription: String,
    val imageResId: Int,
    val mapUrl: String
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouristAttractionDetails(
    name: String,
    shortDescription: String,
    longDescription: String,
    imageResId: Int,
    mapUrl: String,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Attraction Details") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .border(BorderStroke(5.dp, Color.LightGray))
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(5.dp, Color.LightGray))
                    .padding(8.dp)
            ) {
                Text(text = name, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(5.dp, Color.LightGray))
                    .padding(8.dp)
            ) {
                Text(text = shortDescription, fontSize = 20.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(5.dp, Color.LightGray))
                    .padding(8.dp)
            ) {
                Text(text = longDescription, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    openGoogleMaps(navController.context, mapUrl)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "View on Map")
            }
        }
    }
}


fun openGoogleMaps(context: Context, mapUrl: String) {
    val mapUri = Uri.parse(mapUrl)
    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
    try {
        context.startActivity(mapIntent)
    } catch (e: ActivityNotFoundException) {
        Log.e("OpenGoogleMaps", "Google Maps app not found. Opening in browser.")
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        context.startActivity(browserIntent)
    }
}

object AttractionsRepository {
    fun getAttractions(): List<TouristAttraction> {
        return listOf(
            TouristAttraction(
                name = "Shifen",
                shortDescription = "The Waterfall of Taiwan",
                longDescription = "Shifen Waterfall is a stunning natural wonder located in Pingxi District. It's a popular spot for viewing the beautiful cascade and lighting sky lanterns.",
                imageResId = R.drawable.attraction1,
                mapUrl = "https://maps.app.goo.gl/gVWE7szb452rKqnZA"
            ),
            TouristAttraction(
                name = "Ximen",
                shortDescription = "Shopping district",
                longDescription = "Ximen is a bustling shopping district in Taipei, famous for its vibrant street life, trendy shops, and entertainment options. It's a hub for youth culture and fashion.",
                imageResId = R.drawable.ximen,
                mapUrl = "https://maps.app.goo.gl/oN6GxbBCD1PFtgZP8"
            ),
            TouristAttraction(
                name = "Chiang Kai Shek Memorial Hall",
                shortDescription = "History",
                longDescription = "This historical landmark in Taipei honors Chiang Kai-shek. The memorial hall is surrounded by a large plaza and traditional Chinese gardens.",
                imageResId = R.drawable.cks,
                mapUrl = "https://maps.app.goo.gl/V2tz2MGGwXGYbjCx8"
            ),
            TouristAttraction(
                name = "Taipei 101",
                shortDescription = "Landmark",
                longDescription = "Taipei 101 is an iconic skyscraper in Taipei and one of the tallest buildings in the world. It features a high-speed elevator and an observation deck offering breathtaking views of the city.",
                imageResId = R.drawable.taipei,
                mapUrl = "https://maps.app.goo.gl/aL6ucUkdbkWVCQyN6"
            ),
            TouristAttraction(
                name = "Longshan Temple",
                shortDescription = "Temple",
                longDescription = "Longshan Temple is a historic and culturally significant temple located in Wanhua District, Taipei. It's a place of worship and a testament to Taiwanese religious practices and architecture.",
                imageResId = R.drawable.longshan,
                mapUrl = "https://maps.app.goo.gl/H1fNF23KEAHyohBv8"
            ),
            TouristAttraction(
                name = "Jiufen",
                shortDescription = "Taiwan Old Street",
                longDescription = "Jiufen is a charming old street in Ruifang District, known for its narrow alleyways, traditional teahouses, and beautiful views of the surrounding mountains and ocean.",
                imageResId = R.drawable.jiufen,
                mapUrl = "https://maps.app.goo.gl/Jd8rKsEwQcDSHoRY7"
            ),
            TouristAttraction(
                name = "Taroko National Park",
                shortDescription = "Best natural attraction in Hualien",
                longDescription = "Located in Hualien, Taroko National Park is renowned for its stunning natural landscapes, including marble canyons, lush forests, and picturesque trails.",
                imageResId = R.drawable.taroko,
                mapUrl = "https://maps.app.goo.gl/zcSC6W8XxKyifua3A"
            ),
            TouristAttraction(
                name = "Shilin Night Market",
                shortDescription = "One of the best night market in Taiwan",
                longDescription = "Shilin Night Market is one of the most famous night markets in Taiwan, offering a wide variety of delicious street food, souvenirs, and entertainment.",
                imageResId = R.drawable.shilin,
                mapUrl = "https://maps.app.goo.gl/xbqiPq883tNBB2qT6"
            ),
            TouristAttraction(
                name = "YangMingShan National Park",
                shortDescription = "Places where we can observe the beauty of mountains",
                longDescription = "Yangmingshan National Park is located near Taipei and is known for its hot springs, volcanic landscapes, and beautiful mountain scenery. It's a great place for hiking and nature observation.",
                imageResId = R.drawable.yangmingshan,
                mapUrl = "https://maps.app.goo.gl/zcSC6W8XxKyifua3A"
            ),
            TouristAttraction(
                name = "Sun Moon Lake",
                shortDescription = "One of the best lake in Taichung",
                longDescription = "Sun Moon Lake, located in Taichung, is the largest body of water in Taiwan. It's famous for its picturesque views, boating activities, and the surrounding scenic trails.",
                imageResId = R.drawable.sunmoonlake,
                mapUrl = "https://maps.app.goo.gl/KfaJ6iGka6Je861D9"
            ),
        )
    }
}

@Preview
@Composable
fun TouristAttractionsListPreview() {
    TravelTheme {
        TouristAttractionsList(navController = rememberNavController())
    }
}

@Preview
@Composable
fun TouristAttractionDetailsPreview() {
    val navController = rememberNavController()
    TravelTheme {
        TouristAttractionDetails(
            name = "Sample Attraction",
            shortDescription = "This is a description of the sample attraction.",
            longDescription = "This is a detailed description of the sample attraction, providing more information and context.",
            imageResId = R.drawable.attraction1,
            mapUrl = "https://maps.app.goo.gl/gVWE7szb452rKqnZA",
            navController = navController
        )
    }
}
