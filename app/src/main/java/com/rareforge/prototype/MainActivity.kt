package com.rareforge.prototype

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.roundToInt
import kotlin.random.Random

private data class BattleCard(val id: Int, val name: String, val role: String, val icon: String,
    val baseHp: Int, val baseDamage: Int, val attackSeconds: Float, val speed: Float, val cost: Int)

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("rareforge_save_v2", MODE_PRIVATE) }
    private val levels = mutableMapOf<Int, Int>()
    private val cards: List<BattleCard> by lazy { buildCardCatalog() }
    private var boost = 2.0f
    private var query = ""
    private var visibleLimit = 60
    private var selectedCardId = 0
    private lateinit var root: LinearLayout
    private lateinit var collection: LinearLayout
    private lateinit var countLabel: TextView
    private lateinit var battleResult: TextView
    private lateinit var detailPanel: LinearLayout
    private lateinit var boostLabel: TextView
    private lateinit var searchBox: EditText

    private val mint = Color.rgb(95, 220, 194)
    private val muted = Color.rgb(184, 196, 218)
    private val pageBackground = Color.rgb(12, 18, 35)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = pageBackground
        window.navigationBarColor = pageBackground
        cards.forEach { card -> levels[card.id] = prefs.getInt("level_${card.id}", 5).coerceAtLeast(1) }
        selectedCardId = prefs.getInt("selected_card", 0).coerceIn(0, cards.lastIndex)
        buildUi()
    }

    private fun buildCardCatalog(): List<BattleCard> {
        val originals = listOf(
            BattleCard(0, "Ember Guard", "Frontline • Melee", "🔥", 720, 105, 1.2f, 1.0f, 3),
            BattleCard(1, "Mossback", "Tank • Melee", "🌿", 1250, 78, 1.6f, .65f, 4),
            BattleCard(2, "Storm Slinger", "Ranged • Air & ground", "⚡", 440, 150, 1.7f, .9f, 4),
            BattleCard(3, "Frost Adept", "Ranged • Slow", "❄️", 390, 92, 1.1f, .8f, 3),
            BattleCard(4, "Sun Cleric", "Support • Healer", "☀️", 510, 65, 1.4f, .8f, 3),
            BattleCard(5, "Bombardier", "Splash • Ground", "💣", 470, 175, 2.0f, .7f, 4),
            BattleCard(6, "Rift Hound", "Fast • Melee", "🐺", 560, 118, .9f, 1.45f, 3),
            BattleCard(7, "Crystal Archer", "Ranged • Long range", "💎", 360, 125, 1.3f, .85f, 3),
            BattleCard(8, "Ironhorn", "Bruiser • Charge", "🦬", 980, 145, 1.8f, .75f, 5),
            BattleCard(9, "Gale Twins", "Swarm • Air & ground", "🌪️", 260, 58, .8f, 1.3f, 3),
            BattleCard(10, "Lava Sprout", "Siege • Area damage", "🌋", 650, 185, 2.2f, .55f, 5),
            BattleCard(11, "Moon Trickster", "Disruptor • Ranged", "🌙", 420, 105, 1.0f, 1.0f, 3)
        )
        val prefixes = listOf("Ashen", "Astral", "Bramble", "Celestial", "Dusk", "Elder", "Feral", "Glacial", "Hollow", "Ivory", "Jade", "Kindled", "Lunar", "Mystic", "Nova", "Obsidian", "Primal", "Quartz", "Runic", "Solar")
        val nouns = listOf("Guardian", "Archer", "Warden", "Beast", "Mage", "Rider", "Sentinel", "Ranger", "Golem", "Invoker", "Stalker", "Oracle", "Brawler", "Elemental", "Drake", "Alchemist", "Knight", "Seer", "Vanguard", "Phantom", "Druid")
        val icons = listOf("🛡️", "🏹", "🧙", "🐉", "🗡️", "✨", "🦅", "🪨", "🌊", "🌩️", "🌱", "🔮")
        val roles = listOf("Frontline", "Ranged", "Tank", "Support", "Skirmisher", "Siege", "Disruptor", "Air unit")
        val generated = (0 until 420).map { index ->
            val id = originals.size + index
            val hp = 300 + ((index * 73) % 1050) + (index / 420) * 7
            val damage = 45 + ((index * 37) % 190) + index / 420
            BattleCard(id, "${prefixes[index % prefixes.size]} ${nouns[(index / prefixes.size) % nouns.size]} ${index + 1}",
                roles[index % roles.size], icons[index % icons.size], hp, damage, .7f + (index % 16) * .1f,
                .55f + (index % 12) * .1f, 2 + index % 5)
        }
        return originals + generated
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(pageBackground); isFillViewport = true }
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(18), dp(16), dp(28)) }
        scroll.addView(root)
        setContentView(scroll)

        addText("RAREFORGE • OFFLINE SANDBOX", 12f, mint, true, bottom = 5)
        addText("Collection Overdrive", 29f, Color.WHITE, true, bottom = 5)
        addText("Standalone original card-battle prototype. Progress is saved on this device; it does not modify or connect to the official game.", 14f, muted, false, bottom = 14)

        val economy = panel()
        economy.addView(text("SANDBOX ACCOUNT", 12f, mint, true))
        economy.addView(text("Coins  ∞     Gems  ∞     Cards unlocked  ${cards.size}", 18f, Color.WHITE, true).apply { setPadding(0, dp(7), 0, dp(4)) })
        economy.addView(text("Unlimited local sandbox currency • every catalog card is available • upgrades save automatically", 13f, muted, false))
        root.addView(economy, marginBottom(12))

        val boostPanel = panel()
        boostPanel.addView(text("GLOBAL STAT BOOST", 12f, mint, true))
        boostLabel = text("2.00× multiplier", 21f, Color.WHITE, true).apply { setPadding(0, dp(5), 0, dp(2)) }
        boostPanel.addView(boostLabel)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        controls.addView(button("−").apply { setOnClickListener { setBoost((boost - .25f).coerceAtLeast(1f)) } }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { marginEnd = dp(8) })
        controls.addView(button("+").apply { setOnClickListener { setBoost((boost + .25f).coerceAtMost(5f)) } }, LinearLayout.LayoutParams(0, dp(44), 1f))
        boostPanel.addView(controls)
        boostPanel.addView(text("Adjust HP and damage from 1.0× to 5.0×.", 12f, muted, false).apply { setPadding(0, dp(4), 0, 0) })
        root.addView(boostPanel, marginBottom(12))

        val battle = panel()
        battle.addView(text("BATTLE ARENA", 12f, mint, true))
        battle.addView(text("Run a local roster-vs-roster battle using card stats, attack rate, speed, and levels.", 13f, muted, false).apply { setPadding(0, dp(5), 0, dp(9)) })
        battle.addView(button("FIGHT A RANDOM RIVAL").apply { setOnClickListener { runBattle() } })
        battleResult = text("Choose Fight to test your roster.", 14f, Color.WHITE, true).apply { setPadding(0, dp(10), 0, 0) }
        battle.addView(battleResult)
        root.addView(battle, marginBottom(16))

        addText("CARD COLLECTION", 12f, mint, true, bottom = 4)
        countLabel = addText("Loading collection…", 13f, muted, false, bottom = 8)
        searchBox = EditText(this).apply {
            hint = "Search 432 cards by name or role…"
            setSingleLine(true); textSize = 15f; setTextColor(Color.WHITE); setHintTextColor(Color.rgb(125, 143, 170))
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = roundBg(Color.rgb(22, 32, 54), 12, Color.rgb(42, 58, 85))
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { query = s?.toString()?.trim().orEmpty(); visibleLimit = 60; renderCollection() }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        root.addView(searchBox, marginBottom(10))
        detailPanel = panel()
        root.addView(detailPanel, marginBottom(12))
        collection = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(collection)
        renderDetail()
        renderCollection()
        addText("Created by Satyendra Kumar Namdeo • Independent prototype.", 11f, Color.rgb(120, 137, 164), false, top = 18)
    }

    private fun setBoost(value: Float) {
        boost = (value * 100).roundToInt() / 100f
        boostLabel.text = String.format("%.2f× multiplier", boost)
        renderDetail()
        renderCollection()
    }

    private fun renderDetail() {
        if (!::detailPanel.isInitialized) return
        detailPanel.removeAllViews()
        val card = cards[selectedCardId.coerceIn(0, cards.lastIndex)]
        val level = levels[card.id] ?: 5
        detailPanel.addView(text("SELECTED CARD • LEVEL $level", 11f, mint, true))
        detailPanel.addView(text("${card.icon}  ${card.name}", 21f, Color.WHITE, true).apply { setPadding(0, dp(5), 0, dp(5)) })
        detailPanel.addView(text("${card.role}   •   Unlocked", 13f, muted, false))
        val factor = 1.0 + (level - 1) * .08
        detailPanel.addView(text("HP  ${(card.baseHp * boost * factor).roundToInt()}     Damage  ${(card.baseDamage * boost * factor).roundToInt()}", 15f, Color.WHITE, true).apply { setPadding(0, dp(8), 0, dp(3)) })
        detailPanel.addView(text("Attack interval: ${String.format("%.1f", card.attackSeconds)}s  •  Speed: ${String.format("%.2f", card.speed)}  •  Cost: ${card.cost}", 12f, muted, false))
        val upgrade = button(if (level >= 100) "MAX LEVEL REACHED" else "UPGRADE LEVEL • FREE")
        upgrade.isEnabled = level < 100
        upgrade.alpha = if (level >= 100) .55f else 1f
        upgrade.setOnClickListener {
            val next = ((levels[card.id] ?: 5) + 1).coerceAtMost(100)
            levels[card.id] = next
            prefs.edit().putInt("level_${card.id}", next).putInt("selected_card", card.id).apply()
            renderDetail(); renderCollection()
            Toast.makeText(this, "${card.name} upgraded to Level $next", Toast.LENGTH_SHORT).show()
        }
        detailPanel.addView(upgrade)
    }

    private fun renderCollection() {
        if (!::collection.isInitialized) return
        collection.removeAllViews()
        val filtered = cards.filter { it.name.contains(query, true) || it.role.contains(query, true) }
        countLabel.text = "${filtered.size} cards found • all unlocked • showing up to ${minOf(visibleLimit, filtered.size)}"
        filtered.take(visibleLimit).forEach { card ->
            val level = levels[card.id] ?: 5
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                background = roundBg(if (card.id == selectedCardId) Color.rgb(32, 57, 75) else Color.rgb(22, 32, 54), 14, if (card.id == selectedCardId) mint else Color.rgb(42, 58, 85))
                isClickable = true; isFocusable = true
                setOnClickListener { selectedCardId = card.id; prefs.edit().putInt("selected_card", card.id).apply(); renderDetail(); renderCollection() }
            }
            row.addView(text(card.icon, 25f, Color.WHITE, true).apply { gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)) })
            val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), 0, dp(4), 0) }
            labels.addView(text(card.name, 14f, Color.WHITE, true))
            labels.addView(text("Lv $level • ${card.role}", 11f, muted, false).apply { setPadding(0, dp(2), 0, 0) })
            row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(text("HP ${(card.baseHp * boost * (1.0 + (level - 1) * .08)).roundToInt()}", 11f, mint, true))
            collection.addView(row, marginBottom(6))
        }
        if (filtered.size > visibleLimit) collection.addView(button("LOAD NEXT ${minOf(60, filtered.size - visibleLimit)} CARDS").apply { setOnClickListener { visibleLimit += 60; renderCollection() } }, marginBottom(8))
        if (filtered.isEmpty()) collection.addView(text("No cards match that search. Try a different name or role.", 14f, muted, false))
    }

    private fun runBattle() {
        val team = cards.shuffled().take(4)
        val rival = cards.shuffled().take(4)
        fun power(roster: List<BattleCard>, player: Boolean): Double = roster.sumOf { card ->
            val level = if (player) (levels[card.id] ?: 5) else Random.nextInt(3, 16)
            val factor = 1.0 + (level - 1) * .08
            val dps = card.baseDamage * boost * factor / card.attackSeconds
            val hp = card.baseHp * boost * factor
            hp * .45 + dps * 16 + card.speed * 45
        }
        val yours = power(team, true)
        val theirs = power(rival, false)
        val won = yours >= theirs
        val margin = (kotlin.math.abs(yours - theirs) / maxOf(yours, theirs) * 100).roundToInt()
        battleResult.text = "${if (won) "VICTORY" else "DEFEAT"} • power margin $margin%\\nYour team: ${team.joinToString(", ") { it.name }}\\nRival team: ${rival.joinToString(", ") { it.name }}\\nOffline stat-based simulation; no online match or official account is involved."
    }

    private fun panel() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14))
        background = roundBg(Color.rgb(22, 32, 54), 18, Color.rgb(42, 58, 85))
    }

    private fun addText(value: String, size: Float, color: Int, bold: Boolean, top: Int = 0, bottom: Int = 0): TextView =
        text(value, size, color, bold).also { it.setPadding(0, dp(top), 0, dp(bottom)); root.addView(it) }

    private fun text(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
        setLineSpacing(dp(2).toFloat(), 1.0f)
    }

    private fun button(label: String) = Button(this).apply {
        text = label; textSize = 12f; setTextColor(pageBackground); background = roundBg(mint, 12)
        isAllCaps = false; typeface = Typeface.DEFAULT_BOLD; minHeight = dp(44); setPadding(dp(10), 0, dp(10), 0)
    }

    private fun roundBg(color: Int, radius: Int, strokeColor: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
        if (strokeColor != null) setStroke(dp(1), strokeColor)
    }

    private fun marginBottom(value: Int) = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(value) }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
