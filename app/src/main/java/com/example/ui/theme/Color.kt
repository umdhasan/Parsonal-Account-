package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Editorial Botanical Emerald & Warm Paper Palette
val EmeraldPrimary = Color(0xFF0D6E52)
val EmeraldDeep = Color(0xFF094735)
val EmeraldContainer = Color(0xFFD2F3E6)
val OnEmeraldContainer = Color(0xFF052E21)

val WarmGold = Color(0xFFC48B22)
val WarmGoldContainer = Color(0xFFFDF1D6)
val OnWarmGoldContainer = Color(0xFF3E2900)

val SlateTeal = Color(0xFF285F6B)
val SlateTealContainer = Color(0xFFD5EFF5)
val OnSlateTealContainer = Color(0xFF082D35)

// Light Surfaces — Warm Ivory Ledger Paper
val IvoryBackground = Color(0xFFF7F6F1)
val CrispSurface = Color(0xFFFFFFFF)
val WarmSurfaceVariant = Color(0xFFEDEBE3)
val WarmSurfaceContainerLow = Color(0xFFFBFBF8)
val WarmSurfaceContainer = Color(0xFFF3F1EA)
val WarmSurfaceContainerHigh = Color(0xFFEAE7DD)
val InkPrimary = Color(0xFF151E1B)
val InkSecondary = Color(0xFF4A5852)
val HairlineOutline = Color(0xFFD5D1C5)
val HairlineOutlineVariant = Color(0xFFE5E2D8)

// Dark Surfaces — Deep Forest Obsidian
val ObsidianBackground = Color(0xFF0D1412)
val ObsidianSurface = Color(0xFF131D1A)
val ObsidianSurfaceContainerLow = Color(0xFF16211E)
val ObsidianSurfaceContainer = Color(0xFF1C2925)
val ObsidianSurfaceContainerHigh = Color(0xFF24342F)
val ObsidianOutline = Color(0xFF354841)
val ObsidianOutlineVariant = Color(0xFF24332E)
val EmeraldLightAccent = Color(0xFF56D8AC)
val EmeraldDarkContainer = Color(0xFF0B4F3A)
val GoldLightAccent = Color(0xFFF2C96B)
val CloudInk = Color(0xFFE8F1ED)
val MutedCloudInk = Color(0xFFA6BAB2)

// Semantic Finance Tokens
val IncomeGreenLight = Color(0xFF0B875B)
val IncomeGreenContainerLight = Color(0xFFE3F7EE)
val IncomeGreenDark = Color(0xFF4FE0AA)
val IncomeGreenContainerDark = Color(0xFF0E382A)

val ExpenseCoralLight = Color(0xFFC83E37)
val ExpenseCoralContainerLight = Color(0xFFFDECEB)
val ExpenseCoralDark = Color(0xFFFF7A72)
val ExpenseCoralContainerDark = Color(0xFF421A19)

val LoanGivenBlueLight = Color(0xFF1C5F94)
val LoanGivenContainerLight = Color(0xFFE5F1FB)
val LoanGivenBlueDark = Color(0xFF72B9F5)
val LoanGivenContainerDark = Color(0xFF14314A)

val LoanTakenAmberLight = Color(0xFFB56712)
val LoanTakenContainerLight = Color(0xFFFEF2E1)
val LoanTakenAmberDark = Color(0xFFF5B258)
val LoanTakenContainerDark = Color(0xFF3F2B0D)

val HeroGradientStart = Color(0xFF083B2C)
val HeroGradientMid = Color(0xFF0F5E46)
val HeroGradientEnd = Color(0xFF13483A)

@Immutable
data class LedgerExtendedColors(
    val income: Color,
    val incomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val loanGiven: Color,
    val loanGivenContainer: Color,
    val loanTaken: Color,
    val loanTakenContainer: Color,
    val goldAccent: Color,
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,
    val cardBorder: Color,
)

val LightLedgerColors = LedgerExtendedColors(
    income = IncomeGreenLight,
    incomeContainer = IncomeGreenContainerLight,
    expense = ExpenseCoralLight,
    expenseContainer = ExpenseCoralContainerLight,
    loanGiven = LoanGivenBlueLight,
    loanGivenContainer = LoanGivenContainerLight,
    loanTaken = LoanTakenAmberLight,
    loanTakenContainer = LoanTakenContainerLight,
    goldAccent = WarmGold,
    heroStart = HeroGradientStart,
    heroMid = HeroGradientMid,
    heroEnd = HeroGradientEnd,
    cardBorder = HairlineOutlineVariant,
)

val DarkLedgerColors = LedgerExtendedColors(
    income = IncomeGreenDark,
    incomeContainer = IncomeGreenContainerDark,
    expense = ExpenseCoralDark,
    expenseContainer = ExpenseCoralContainerDark,
    loanGiven = LoanGivenBlueDark,
    loanGivenContainer = LoanGivenContainerDark,
    loanTaken = LoanTakenAmberDark,
    loanTakenContainer = LoanTakenContainerDark,
    goldAccent = GoldLightAccent,
    heroStart = Color(0xFF072B21),
    heroMid = Color(0xFF0D4735),
    heroEnd = Color(0xFF0B3228),
    cardBorder = ObsidianOutlineVariant,
)

val LocalLedgerColors = staticCompositionLocalOf { LightLedgerColors }
