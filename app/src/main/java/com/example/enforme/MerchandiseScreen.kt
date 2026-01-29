package com.example.enforme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlin.math.max

data class MerchProduct(
    val id: String,
    val name: String,
    val description: String,
    val priceNaira: Int,
    val tag: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchandiseScreen(
    // Later, we’ll wire this to OnePipe (send_invoice:virtual_account or collect)
    onCheckout: ((totalKobo: Int, items: List<Pair<MerchProduct, Int>>) -> Unit)? = null
) {
    val context = LocalContext.current
    val products = remember { merchProducts() }

    // cart: productId -> qty
    val cart = remember { mutableStateMapOf<String, Int>() }

    val cartCount by remember {
        derivedStateOf { cart.values.sum() }
    }

    val cartItems by remember {
        derivedStateOf {
            cart.entries.mapNotNull { (id, qty) ->
                val p = products.firstOrNull { it.id == id } ?: return@mapNotNull null
                p to qty
            }
        }
    }

    val totalNaira by remember {
        derivedStateOf { cartItems.sumOf { (p, qty) -> p.priceNaira * qty } }
    }

    var showCartSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Merchandise", fontWeight = FontWeight.Bold) },
                actions = {
                    BadgedBox(
                        badge = { if (cartCount > 0) Badge { Text(cartCount.toString()) } }
                    ) {
                        IconButton(onClick = { showCartSheet = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = products,
                key = { it.id } // ✅ helps performance + less choppy
            ) { product ->
                MerchCard(
                    product = product,
                    imageRequest = remember(product.imageUrl) {
                        ImageRequest.Builder(context)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .build()
                    },
                    onAdd = {
                        val current = cart[product.id] ?: 0
                        cart[product.id] = current + 1
                    }
                )
            }

            item { Spacer(Modifier.height(60.dp)) }
        }

        if (showCartSheet) {
            ModalBottomSheet(onDismissRequest = { showCartSheet = false }) {
                CartSheet(
                    cartItems = cartItems,
                    totalNaira = totalNaira,
                    onIncrease = { product ->
                        val current = cart[product.id] ?: 0
                        cart[product.id] = current + 1
                    },
                    onDecrease = { product ->
                        val current = cart[product.id] ?: 0
                        val next = max(0, current - 1)
                        if (next == 0) cart.remove(product.id) else cart[product.id] = next
                    },
                    onCheckout = {
                        val totalKobo = totalNaira * 100
                        onCheckout?.invoke(totalKobo, cartItems)
                        showCartSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MerchCard(
    product: MerchProduct,
    imageRequest: ImageRequest,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BrokenImage,
                            contentDescription = "Image failed",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Column(Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(onClick = {}, label = { Text(product.tag) })
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatNaira(product.priceNaira),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onAdd,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add to Cart")
                    }
                }
            }
        }
    }
}

@Composable
private fun CartSheet(
    cartItems: List<Pair<MerchProduct, Int>>,
    totalNaira: Int,
    onIncrease: (MerchProduct) -> Unit,
    onDecrease: (MerchProduct) -> Unit,
    onCheckout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 26.dp)
    ) {
        Text("Your Cart", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (cartItems.isEmpty()) {
            Text(
                "Your cart is empty.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            return
        }

        cartItems.forEach { (p, qty) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(p.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${formatNaira(p.priceNaira)} × $qty",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onDecrease(p) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(qty.toString(), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { onIncrease(p) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
            Divider()
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(formatNaira(totalNaira), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Checkout")
        }
    }
}

private fun formatNaira(amount: Int): String = "₦" + "%,d".format(amount)

private fun merchProducts(): List<MerchProduct> = listOf(
    MerchProduct(
        id = "p1",
        name = "En Forme Performance T-Shirt",
        description = "Breathable moisture-wicking fabric for optimal workout comfort",
        priceNaira = 8500,
        tag = "Apparel",
        // ✅ replaced (stable)
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d8/Fight_Corona_Virus_T-shirt_in_Kannada.jpg"
    ),
    MerchProduct(
        id = "p2",
        name = "Premium Water Bottle",
        description = "1L insulated stainless steel bottle keeps drinks cold for 24 hours",
        priceNaira = 3500,
        tag = "Accessories",
        // ✅ replaced (stable)
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/e/e4/Reusable_Steel_Water_Bottles.jpg"
    ),
    MerchProduct(
        id = "p3",
        name = "Elite Gym Bag",
        description = "Spacious duffle with separate shoe compartment and laptop sleeve",
        priceNaira = 15000,
        tag = "Accessories",
        // ✅ replaced (stable)
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/6/6a/Leather_duffel_bag_on_the_ground_%28Unsplash%29.jpg"
    ),
    MerchProduct(
        id = "p4",
        name = "Premium Yoga Mat",
        description = "Non-slip eco-friendly mat with extra cushioning and carrying strap",
        priceNaira = 12000,
        tag = "Equipment",
        // ✅ replaced (stable)
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/Woman_on_a_yoga_mat_next_to_a_window_doing_lower_back_exercises_-_50401795697.jpg/1280px-Woman_on_a_yoga_mat_next_to_a_window_doing_lower_back_exercises_-_50401795697.jpg"
    ),
    MerchProduct(
        id = "p5",
        name = "Resistance Band Set",
        description = "Complete set with 5 resistance levels for versatile training",
        priceNaira = 9000,
        tag = "Equipment",
        // (this one was working for you before; keeping it)
        imageUrl = "https://images.unsplash.com/photo-1599058917212-d750089bc07e?auto=format&fit=crop&w=1400&q=80"
    ),
    MerchProduct(
        id = "p6",
        name = "Whey Protein Powder",
        description = "2kg premium isolate protein with 25g per serving - chocolate flavor",
        priceNaira = 25000,
        tag = "Supplements",
        // ✅ replaced (stable)
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0b/Whey_Protein_Isolate.jpg"
    )
)
