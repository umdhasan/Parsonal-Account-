package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object LedgerCategoryVisuals {

    fun iconForCategory(category: String): ImageVector = when (category.trim().lowercase()) {
        "salary" -> Icons.Rounded.Work
        "business" -> Icons.Rounded.Storefront
        "freelance" -> Icons.Rounded.LaptopMac
        "bonus" -> Icons.Rounded.Stars
        "loan return", "loan paid" -> Icons.Rounded.SwapHoriz
        "gift" -> Icons.Rounded.CardGiftcard
        "food" -> Icons.Rounded.Restaurant
        "transport" -> Icons.Rounded.DirectionsBus
        "bills" -> Icons.Rounded.ReceiptLong
        "rent" -> Icons.Rounded.Home
        "shopping" -> Icons.Rounded.ShoppingBag
        "health" -> Icons.Rounded.LocalHospital
        "family" -> Icons.Rounded.FamilyRestroom
        "education" -> Icons.Rounded.School
        else -> Icons.Rounded.AccountBalanceWallet
    }

    fun iconForPaymentMethod(method: String): ImageVector = when (method.trim().lowercase()) {
        "bank" -> Icons.Rounded.AccountBalance
        "card" -> Icons.Rounded.CreditCard
        "bkash / mobile" -> Icons.Rounded.PhoneAndroid
        else -> Icons.Rounded.Payments
    }

    val chartPalette = listOf(
        Color(0xFF0D6E52),
        Color(0xFFC83E37),
        Color(0xFFC48B22),
        Color(0xFF1C5F94),
        Color(0xFF7B4B94),
        Color(0xFF2B8A9C),
        Color(0xFFD66A2D),
        Color(0xFF5D7A4F),
    )
}
