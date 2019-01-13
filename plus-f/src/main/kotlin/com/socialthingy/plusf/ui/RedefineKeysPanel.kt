package com.socialthingy.plusf.ui

import com.socialthingy.plusf.spectrum.UserPreferences
import com.socialthingy.plusf.spectrum.UserPreferences.*
import java.awt.GridLayout
import java.awt.event.*
import java.awt.event.KeyEvent.*
import javax.swing.*
import javax.swing.JComponent.WHEN_FOCUSED

data class JoystickKeys(val up: Int, val down: Int, val left: Int, val right: Int, val fire: Int) {
    constructor(prefs: UserPreferences) : this(
        prefs.getOrElse(JOYSTICK_UP, VK_Q),
        prefs.getOrElse(JOYSTICK_DOWN, VK_A),
        prefs.getOrElse(JOYSTICK_LEFT, VK_O),
        prefs.getOrElse(JOYSTICK_RIGHT, VK_P),
        prefs.getOrElse(JOYSTICK_FIRE, VK_M)
    )

    fun save(prefs: UserPreferences) {
        prefs.set(JOYSTICK_UP, up.toString())
        prefs.set(JOYSTICK_DOWN, down.toString())
        prefs.set(JOYSTICK_LEFT, left.toString())
        prefs.set(JOYSTICK_RIGHT, right.toString())
        prefs.set(JOYSTICK_FIRE, fire.toString())
    }
}

class RedefineKeysPanel(initialJoystickKeys: JoystickKeys) : JPanel(), KeyListener {
    private val up = RedefinableKey("Up", initialJoystickKeys.up)
    private val down = RedefinableKey("Down", initialJoystickKeys.down)
    private val left = RedefinableKey("Left", initialJoystickKeys.left)
    private val right = RedefinableKey("Right", initialJoystickKeys.right)
    private val fire = RedefinableKey("Fire", initialJoystickKeys.fire)
    private val allKeys = listOf(up, down, left, right, fire)

    private var currentKey: RedefinableKey? = null

    init {
        layout = GridLayout(5, 2, 2, 2)

        allKeys.forEach { key ->
            add(key.label)
            add(key.button)

            key.button.addActionListener {
                if (key != currentKey) {
                    currentKey?.hidePrompt()
                    currentKey = key
                    key.showPrompt()
                }
            }
            key.button.addKeyListener(this)
        }
    }

    fun getKeys(): JoystickKeys = JoystickKeys(up.value, down.value, left.value, right.value, fire.value)

    override fun keyTyped(e: KeyEvent?) {
    }

    override fun keyPressed(e: KeyEvent?) {
    }

    override fun keyReleased(e: KeyEvent?) {
        if (e != null) {
            if (currentKey != null) {
                currentKey?.hidePrompt()
                currentKey?.value = e.keyCode
            }
            currentKey = null
        }
    }
}

private class RedefinableKey(labelText: String, initialValue: Int) {
    private val spacePressedKeyStroke = KeyStroke.getKeyStroke("SPACE")
    private val spaceReleasedKeyStroke = KeyStroke.getKeyStroke("released SPACE")

    val label = JLabel(labelText)
    val button = JButton(KeyEvent.getKeyText(initialValue))
    var value = initialValue
        set(value) {
            field = value
            button.text = KeyEvent.getKeyText(value)
        }

    fun showPrompt() {
        button.getInputMap(WHEN_FOCUSED).put(spacePressedKeyStroke, "")
        button.getInputMap(WHEN_FOCUSED).put(spaceReleasedKeyStroke, "")
        button.text = "?"
    }

    fun hidePrompt() {
        button.text = KeyEvent.getKeyText(value)
        button.getInputMap(WHEN_FOCUSED).put(spacePressedKeyStroke, "pressed")
        button.getInputMap(WHEN_FOCUSED).put(spaceReleasedKeyStroke, "released")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedefinableKey
        return label == other.label
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }
}