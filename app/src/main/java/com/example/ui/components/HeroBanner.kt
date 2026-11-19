package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandYellowBright
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HeroBanner(
    onOpenDeals: () -> Unit = {},
    products: List<Product> = emptyList(),
    cart: Map<String, CartItem> = emptyMap(),
    onAddToCart: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val phone = "01002047073"

    val deals = remember(products) {
        val flashDeals = products.filter {
            it.isAvailable && (it.isFlashDeal || it.originalPrice > it.price)
        }
        if (flashDeals.isNotEmpty()) flashDeals
        else products.filter { it.isAvailable }.take(5)
    }

    var currentIdx by remember { mutableIntStateOf(0) }
    if (deals.isNotEmpty() && currentIdx >= deals.size) {
        currentIdx = 0
    }

    LaunchedEffect(deals.size, currentIdx) {
        if (deals.size > 1) {
            delay(4000L)
            currentIdx = (currentIdx + 1) % deals.size
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            BrandDark,
                            Color(0xFF1E3A8A),
                            BrandRed
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Promotional tag with clickable Deals trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = BrandYellowBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "عروض طازجة يومياً 🔥",
                                color = BrandYellowBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = BrandRed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { onOpenDeals() }
                            .testTag("hero_deals_shortcut")
                    ) {
                        Text(
                            text = "قسم سوبر التوفير ⚡",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Heading
                Text(
                    text = "أقوى خصومات السوبرماركت واحتياجات بيتك بأسعار زمان",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 22.sp
                )

                // Subtitle
                Text(
                    text = "اطلب الآن احتياجاتك من الخضار، الفواكه، الألبان، والتموين وتوصلك لحد باب البيت بكل أمان.",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                // Rotating Discount Offers Showcase inside Hero Banner
                if (deals.isNotEmpty()) {
                    val activeProduct = deals[currentIdx.coerceIn(0, deals.lastIndex)]
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .testTag("hero_deals_showcase")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Carousel Header & Nav Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = BrandRed,
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = "🔥 عروض الخصم اليومية",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${currentIdx + 1} / ${deals.size}",
                                        color = BrandYellowBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (deals.size > 1) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .clickable {
                                                    currentIdx = (currentIdx - 1 + deals.size) % deals.size
                                                }
                                                .testTag("hero_deal_prev"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "العرض السابق",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .clickable {
                                                    currentIdx = (currentIdx + 1) % deals.size
                                                }
                                                .testTag("hero_deal_next"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = "العرض التالي",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Animated Deal Slide Card
                            AnimatedContent(
                                targetState = activeProduct,
                                transitionSpec = {
                                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                                        (slideOutHorizontally { -it / 4 } + fadeOut())
                                },
                                label = "hero_deal_slide"
                            ) { prod ->
                                val hasOldPrice = prod.originalPrice > prod.price
                                val savings = if (hasOldPrice) prod.originalPrice - prod.price else 0.0
                                val pctOff = if (hasOldPrice && prod.originalPrice > 0) {
                                    ((savings / prod.originalPrice) * 100).roundToInt()
                                } else 0
                                val badgeText = when {
                                    prod.dealBadge.isNotBlank() -> prod.dealBadge
                                    savings > 0 -> "وفر ${savings.roundToInt()} ج.م ⚡"
                                    else -> "عرض خاص 🔥"
                                }
                                val inCartQty = cart[prod.id]?.quantity ?: 0

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(14.dp),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Product Image with Discount Tag
                                        Box(
                                            modifier = Modifier
                                                .size(82.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF3F4F6))
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(prod.imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = prod.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Surface(
                                                color = BrandRed,
                                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Text(
                                                    text = if (pctOff > 0) "خصم $pctOff%" else "عرض 🔥",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        // Details & Add Button
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Surface(
                                                color = Color(0xFFFEF3C7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    color = Color(0xFF92400E),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = prod.name,
                                                color = BrandDark,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (prod.weight.isNotBlank()) {
                                                Text(
                                                    text = prod.weight,
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.Bottom,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "${String.format(Locale.US, "%.1f", prod.price)} ج.م",
                                                        color = BrandRed,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    if (hasOldPrice) {
                                                        Text(
                                                            text = "${String.format(Locale.US, "%.0f", prod.originalPrice)}",
                                                            color = Color(0xFF9CA3AF),
                                                            fontSize = 11.sp,
                                                            textDecoration = TextDecoration.LineThrough
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    color = if (inCartQty > 0) BrandGreen else BrandRed,
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .clickable { onAddToCart(prod) }
                                                        .testTag("hero_deal_add_${prod.id}")
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AddShoppingCart,
                                                            contentDescription = "أضف للطلب",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (inCartQty > 0) "في السلة ($inCartQty) +" else "أضف للطلب",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Indicator Dots
                            if (deals.size > 1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    deals.forEachIndexed { idx, _ ->
                                        val isActive = idx == currentIdx
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 3.dp)
                                                .height(6.dp)
                                                .width(if (isActive) 18.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isActive) BrandYellowBright
                                                    else Color.White.copy(alpha = 0.4f)
                                                )
                                                .clickable { currentIdx = idx }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Hotline support chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = BrandYellowBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "خدمة العملاء والطلبات السريعة",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = phone,
                            color = BrandYellowBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
