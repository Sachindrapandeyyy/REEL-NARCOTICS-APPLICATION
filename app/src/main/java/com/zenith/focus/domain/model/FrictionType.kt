package com.zenith.focus.domain.model

enum class FrictionType(val displayName: String, val description: String) {
    HOLD_BUTTON("Hold 5 Seconds", "Press and hold a smooth radial button for 5 seconds to unlock."),
    TYPE_PHRASE("Type Mindful Phrase", "Type an anti-impulse statement without pasting to unlock."),
    MATH_TASK("Solve Math Task", "Answer a quick mental arithmetic problem to engage your prefrontal cortex."),
    PIN_CODE("Secure PIN", "Enter your pre-configured 4-digit focus PIN.")
}
