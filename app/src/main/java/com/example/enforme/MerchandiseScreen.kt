package com.example.enforme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

data class MerchProduct(
    val id: String,
    val name: String,
    val description: String,
    val priceNaira: Int, // base price; UI will show FINAL price (rounded up to hundreds)
    val tag: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchandiseScreen(
    // ✅ Keep your existing signature so HomeScreen.kt doesn't need big changes
    onCheckout: ((totalKobo: Int, items: List<Pair<MerchProduct, Int>>) -> Unit)? = null
) {
    val context = LocalContext.current
    val products = remember { merchProducts() }
    val cart = remember { mutableStateMapOf<String, Int>() }

    val cartCount by remember { derivedStateOf { cart.values.sum() } }

    val cartItems by remember {
        derivedStateOf {
            cart.entries.mapNotNull { (id, qty) ->
                val p = products.firstOrNull { it.id == id } ?: return@mapNotNull null
                p to qty
            }
        }
    }

    // ✅ FINAL total in NAIRA (rounded item prices)
    val finalTotalNaira by remember {
        derivedStateOf {
            cartItems.sumOf { (p, qty) -> roundUpToHundreds(p.priceNaira) * qty }
        }
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
            items(products, key = { it.id }) { product ->
                val imageRequest = remember(product.imageUrl) {
                    ImageRequest.Builder(context)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                }

                MerchCard(
                    product = product,
                    imageRequest = imageRequest,
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
                    totalNaira = finalTotalNaira,
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
                        // ✅ what OnePipe gets = FINAL total in KOBO
                        val totalKobo = finalTotalNaira * 100

                        // ✅ send items with FINAL prices too (so backend payload matches UI)
                        val finalizedItems = cartItems.map { (p, qty) ->
                            p.copy(priceNaira = roundUpToHundreds(p.priceNaira)) to qty
                        }

                        onCheckout?.invoke(totalKobo, finalizedItems)
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
    val finalPrice = roundUpToHundreds(product.priceNaira)

    Card(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
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
                        text = formatNaira(finalPrice),
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
            val finalPrice = roundUpToHundreds(p.priceNaira)

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
                        "${formatNaira(finalPrice)} × $qty",
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

// -------------------- money helpers --------------------

private fun roundUpToHundreds(naira: Int): Int {
    if (naira <= 0) return 0
    return (ceil(naira / 100.0) * 100).toInt()
}

private fun formatNaira(amount: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "NG"))
    return "₦${nf.format(amount)}"
}

// -------------------- data --------------------
// Keep base prices here; UI rounds to final “hundreds” so it matches WhatsApp/SMS/Email.

private fun merchProducts(): List<MerchProduct> = listOf(
    MerchProduct(
        id = "p1",
        name = "En Forme Performance T-Shirt",
        description = "Breathable moisture-wicking fabric for optimal workout comfort",
        priceNaira = 85,
        tag = "Apparel",
        imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=1400&q=80"
    ),
    MerchProduct(
        id = "p2",
        name = "Premium Water Bottle",
        description = "1L insulated stainless steel bottle keeps drinks cold for 24 hours",
        priceNaira = 35,
        tag = "Accessories",
        imageUrl = "https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=1400&q=80"
    ),
    MerchProduct(
        id = "p3",
        name = "Elite Gym Bag",
        description = "Spacious duffle with separate shoe compartment and laptop sleeve",
        priceNaira = 150,
        tag = "Accessories",
        imageUrl = "https://images.unsplash.com/photo-1594223274512-ad4803739b7c?auto=format&fit=crop&w=1400&q=80"
    ),
    MerchProduct(
        id = "p4",
        name = "Premium Yoga Mat",
        description = "Non-slip eco-friendly mat with extra cushioning and carrying strap",
        priceNaira = 120,
        tag = "Equipment",
        imageUrl = "https://images.unsplash.com/photo-1593810450967-f9c42742e326?auto=format&fit=crop&w=1400&q=80"
    ),
    MerchProduct(
        id = "p5",
        name = "Resistance Band Set",
        description = "Complete set with 5 resistance levels for versatile training",
        priceNaira = 90,
        tag = "Equipment",
        imageUrl = "https://images.unsplash.com/photo-1594385208974-2e75f8d3a09b?auto=format&fit=crop&w=1400&q=80"
    ),
    MerchProduct(
        id = "p6",
        name = "Whey Protein Powder",
        description = "2kg premium isolate protein with 25g per serving - chocolate flavor",
        priceNaira = 250,
        tag = "Supplements",
        imageUrl = "https://images.unsplash.com/photo-1622480916113-9000e6f273de?auto=format&fit=crop&w=1400&q=80"
    )
)
