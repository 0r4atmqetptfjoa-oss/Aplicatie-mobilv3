package com.example.educationalapp.minigames.shapetrain

import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Shape Train mini‑game.  Holds a list of assignments
 * pairing a shape with a wagon number.  The UI asks the player to place
 * the correct shape into the specified wagon.  When the player selects
 * a shape and taps a wagon, [submitToWagon] will check if it matches
 * the current assignment.
 */
class ShapeTrainGameViewModel : ViewModel() {
    // List of assignments to cycle through; could be randomised for variety.
    val assignments: List<Assignment> = listOf(
        Assignment(ShapeType.CIRCLE, 1),
        Assignment(ShapeType.SQUARE, 2),
        Assignment(ShapeType.TRIANGLE, 3),
        Assignment(ShapeType.RECTANGLE, 4)
    )

    private var selectedShape: ShapeType? = null

    /**
     * Called when the user taps a shape.  Stores the selection so that the next
     * wagon tap can be checked.
     */
    fun selectShape(shape: ShapeType) {
        selectedShape = shape
    }

    /**
     * Called when the user taps a wagon.  Returns true if the selected shape
     * matches the current assignment for this wagon.  Resets the selected
     * shape afterwards.
     */
    fun submitToWagon(wagonNumber: Int): Boolean {
        val assignment = assignments.firstOrNull { it.number == wagonNumber }
        val correct = assignment != null && assignment.shape == selectedShape
        // Reset selection so the user must select again
        selectedShape = null
        return correct
    }
}

/**
 * Represents one assignment: a shape to be placed into a specific wagon.
 */
data class Assignment(val shape: ShapeType, val number: Int)

/**
 * Enum describing the basic shapes used in the Shape Train game.  The [label]
 * property provides a human‑friendly name displayed in the UI.  Additional
 * shapes can be added here if desired.
 */
enum class ShapeType(val label: String) {
    CIRCLE("Cerc"),
    SQUARE("Pătrat"),
    TRIANGLE("Triunghi"),
    RECTANGLE("Dreptunghi")
}