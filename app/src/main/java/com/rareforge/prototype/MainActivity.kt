package com.rareforge.prototype

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import kotlin.math.roundToInt
import kotlin.random.Random

private data class BattleCard(
    val name: String,
    val role: String,
    val icon: String,
    val baseHp: Int,
    val baseDamage: Int,
    val baseAttackSeconds: Float,
    val baseSpeed: Float,
    val elixir: Int
)

class MainActivity : Activity() {
    private val cards = listOf(
        BattleCard("Ember Guard", "Frontline • Melee", "🔥", 720, 105, 1.2f, 1.0f, 3),
        BattleCard("Mossback", "Tank • Melee", "🌿", 1250, 78, 1.6f, 0.65f, 4),
        BattleCard("Storm Slinger", "Ranged • Air & ground", "⚡", 440, 150, 1.7f, 0.9f, 4),
        BattleCard("Frost Adept", "Ranged • Slow effect", "❄️", 390, 92, 1.1f, 0.8f, 3),
        BattleCard("Sun Cleric", "Support • Healer", "☀️", 510, 65, 1.4f, 0.8f, 3),
        BattleCard("Bombardier", "Splash • Ground", "💣", 470, 175, 2.0f, 0.7f, 4),
        BattleCard("Rift Hound", "Fast • Melee", "🐺", 560, 118, 0.9f, 1.45f, 3),
        BattleCard("Crystal Archer", "Ranged • Long range", "💎", 360, 125, 1.3f, 0.85f, 3),
        BattleCard("Ironhorn", "Bruiser • Charge", "🦬", 980, 145, 1.8f, 0.75f, 5),
        BattleCard("Gale Twins", "Swarm • Air & ground", "🌪️", 260, 58, 0.8f, 1.3f, 3),
        BattleCard("Lava Sprout", "Siege • Area damage", "🌋", 650, 185, 2.2f, 0.55f, 5),
        BattleCard("Moon Trickster", "Disruptor • Ranged", "🌙", 420, 105, 1.0f, 1.0f, 3)
    )

    private var boost = 2.0f
    private var resultText: String = "Your training arena is ready. Every card starts unlocked at Level 5."
    private lateinit var content: LinearLayout
    private lateinit var boostValue: TextView
    private lateinit var resultView: TextView
    private lateinit var cardList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 18, 35)
        window.navigationBarColor = Color.rgb(12, 18, 35)
        buildUi()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(12, 18, 35))
            isFillViewport = true
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(28))
        }
        scroll.addView(content)
        setContentView(scroll)

        addText("RAREFORGE", 13f, Color.rgb(95, 220, 194), true, bottom = 4)
        addText("Level 5 • Boost Lab", 29f, Color.WHITE, true, bottom = 5)
        addText(
            "An original offline card-battle sandbox. All 12 prototype Rare units are unlocked; no official game account or server is connected.",
            14f, Color.rgb(184, 196, 218), false, bottom = 16
        )

        val statsPanel = panel()
        statsPanel.addView(text("LOADOUT STATUS", 12f, Color.rgb(95, 220, 194), true))
        statsPanel.addView(text("12 / 12 cards unlocked    •    Level 5", 17f, Color.WHITE, true).apply {
            setPadding(0, dp(7), 0, dp(4))
        })
        statsPanel.addView(text("Boost multiplier applies to hitpoints and damage. Attack interval and movement speed are shown as base stats.", 13f, Color.rgb(184, 196, 218), false))
        content.addView(statsPanel, marginBottom(14))

        val boostPanel = panel()
        boostPanel.addView(text("STAT BOOST", 12f, Color.rgb(95, 220, 194), true))
        boostValue = text("2.0× multiplier", 22f, Color.WHITE, true).apply {
            setPadding(0, dp(6), 0, dp(2))
        }
        boostPanel.addView(boostValue)
        val seek = SeekBar(this).apply {
            max = 40
            progress = 10
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.rgb(95, 220, 194))
            progressTintList = android.content.res.ColorStateList.valueOf(Color.rgb(95, 220, 194))
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    boost = 1.0f + progress / 10f
                    boostValue.text = String.format("%.1f× multiplier", boost)
                    renderCards()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        boostPanel.addView(seek)
        boostPanel.addView(text("Range: 1.0× to 5.0× • Default: 2.0×", 12f, Color.rgb(184, 196, 218), false))
        content.addView(boostPanel, marginBottom(14))

        val battlePanel = panel()
        battlePanel.addView(text("QUICK BATTLE", 12f, Color.rgb(95, 220, 194), true))
        battlePanel.addView(text("Run a lightweight local simulation using your boosted roster.", 14f, Color.rgb(184, 196, 218), false).apply {
            setPadding(0, dp(6), 0, dp(10))
        })
        val battleButton = button("SIMULATE BATTLE")
        battleButton.setOnClickListener { simulateBattle() }
        battlePanel.addView(battleButton)
        resultView = text(resultText, 14f, Color.WHITE, true).apply {
            setPadding(0, dp(12), 0, 0)
        }
        battlePanel.addView(resultView)
        content.addView(battlePanel, marginBottom(18))

        addText("YOUR RARE ROSTER", 12f, Color.rgb(95, 220, 194), true, bottom = 4)
        addText("Tap a card to inspect its boosted stats.", 14f, Color.rgb(184, 196, 218), false, bottom = 10)
        cardList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(cardList)
        renderCards()

        addText("Prototype only • Original names and emoji icons • Not affiliated with Supercell", 11f, Color.rgb(120, 137, 164), false, top = 18)
    }

    private fun renderCards() {
        if (!::cardList.isInitialized) return
        cardList.removeAllViews()
        cards.forEach { card ->
            val box = panel()
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val icon = text(card.icon, 28f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
                background = roundBg(Color.rgb(38, 52, 79), 14)
                layoutParams = LinearLayout.LayoutParams(dp(54), dp(54)).apply { marginEnd = dp(12) }
            }
            header.addView(icon)
            val titleColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            titleColumn.addView(text(card.name, 17f, Color.WHITE, true))
            titleColumn.addView(text("${card.role}  •  Level 5  •  Unlocked", 12f, Color.rgb(95, 220, 194), false).apply {
                setPadding(0, dp(3), 0, 0)
            })
            header.addView(titleColumn, LinearLayout.LayoutParams(0, -2, 1f))
            box.addView(header)

            val hp = (card.baseHp * boost).roundToInt()
            val damage = (card.baseDamage * boost).roundToInt()
            val interval = String.format("%.1f", card.baseAttackSeconds)
            val speed = String.format("%.2f", card.baseSpeed)
            box.addView(text("HP  $hp     Damage  $damage", 14f, Color.WHITE, true).apply {
                setPadding(0, dp(12), 0, dp(3))
            })
            box.addView(text("Attack interval: ${interval}s   •   Move speed: $speed   •   Cost: ${card.elixir}", 12f, Color.rgb(184, 196, 218), false))
            val inspect = button("INSPECT STATS")
            inspect.setOnClickListener {
                resultText = "${card.name}: Level 5 • ${boost}× boost • HP $hp • Damage $damage • Attack interval ${interval}s • Move speed $speed."
                resultView.text = resultText
            }
            box.addView(inspect)
            cardList.addView(box, marginBottom(10))
        }
    }

    private fun simulateBattle() {
        val playerPower = cards.shuffled().take(4).sumOf { (it.baseHp * boost + it.baseDamage * boost * 3).toDouble() }
        val rivalBoost = Random.nextDouble(1.4, 2.3)
        val rivalPower = cards.shuffled().take(4).sumOf { (it.baseHp * rivalBoost + it.baseDamage * rivalBoost * 3).toDouble() }
        val winner = if (playerPower >= rivalPower) "VICTORY" else "DEFEAT"
        val margin = (kotlin.math.abs(playerPower - rivalPower) / maxOf(playerPower, rivalPower) * 100).roundToInt()
        resultText = "$winner  •  Local simulation\nYour roster power: ${playerPower.roundToInt()}  |  Rival power: ${rivalPower.roundToInt()}\nPower difference: $margin%. This is a lightweight prototype simulation, not the official game's battle engine."
        resultView.text = resultText
    }

    private fun panel(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        background = roundBg(Color.rgb(22, 32, 54), 18, Color.rgb(42, 58, 85))
    }

    private fun addText(value: String, size: Float, color: Int, bold: Boolean, top: Int = 0, bottom: Int = 0) {
        content.addView(text(value, size, color, bold).apply {
            setPadding(0, dp(top), 0, dp(bottom))
        })
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setLineSpacing(dp(2).toFloat(), 1.0f)
        }

    private fun button(label: String): Button = Button(this).apply {
        text = label
        textSize = 12f
        setTextColor(Color.rgb(12, 18, 35))
        background = roundBg(Color.rgb(95, 220, 194), 12)
        isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        minHeight = dp(44)
        setPadding(dp(10), 0, dp(10), 0)
    }

    private fun roundBg(color: Int, radius: Int, strokeColor: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != null) setStroke(dp(1), strokeColor)
        }

    private fun marginBottom(value: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(value) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
