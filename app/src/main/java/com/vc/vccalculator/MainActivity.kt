package com.vc.vccalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // These are the TextViews that show what the user types and the result
    private lateinit var workingsTV: TextView
    private lateinit var resultsTV: TextView

    // Flags to control logic
    private var canAddOperation = true  // Prevents multiple operators in a row
    private var canAddDecimal = true    // Prevents multiple decimals in one number
    private var wasEqual = false        // Used to know if equals was just pressed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ensures proper layout with system bars (like status and navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Link our variables to the TextViews in the layout
        workingsTV = findViewById(R.id.workingsTV)
        resultsTV = findViewById(R.id.resultsTV)
    }

    // Utility function to check if a string is numeric
    private fun  isNumericToX(toCheck: String): Boolean {
        return toCheck.toDoubleOrNull() != null
    }

    // Called when a number (or decimal point) button is pressed
    fun numberAction(view: View) {
        if (view is Button) {
            if (wasEqual) {
                // If result was just shown, start new input
                workingsTV.text = ""
                wasEqual = false
            }

            if (view.text == ".") {
                // Only allow one decimal per number
                if (canAddDecimal)
                    workingsTV.append(view.text)
                canAddDecimal = false
            } else {
                workingsTV.append(view.text)
            }

            canAddOperation = true
        }
    }

    // Called when an operator (+, -, x, /) is pressed
    fun operationAction(view: View) {
        if (view is Button && canAddOperation) {
            if (wasEqual) {
                // Start from result if equals was just used
                if (isNumericToX(view.text.toString())) {
                    workingsTV.text = ""
                } else {
                    workingsTV.text = resultsTV.text
                }
                wasEqual = false
            }

            workingsTV.append(view.text)
            canAddDecimal = true
        }
    }

    // Clears all input and results
    fun allClearAction(view: View) {
        workingsTV.text = ""
        resultsTV.text = ""
    }

    // Removes the last character entered
    fun backSpaceAction(view: View) {
        if (wasEqual) {
            // If equals was just pressed, restore result for editing
            workingsTV.text = resultsTV.text
            wasEqual = false
        }

        val length = workingsTV.length()
        if (length > 0)
            workingsTV.text = workingsTV.text.subSequence(0, length - 1)
    }

    // Called when "=" is pressed
    fun equalsAction(view: View) {
        resultsTV.text = calculateResults()
        wasEqual = true
    }

    // Performs full calculation
    private fun calculateResults(): String {
        val digitsOperators = digitsOperators()
        if (digitsOperators.isEmpty()) return ""

        val timesDivision = timesDivisionCalculate(digitsOperators)
        if (timesDivision.isEmpty()) return ""

        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }

    // Calculates all additions and subtractions
    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex) {
                val operator = passedList[i]
                val nextDigit = passedList[i + 1] as Float
                if (operator == '+')
                    result += nextDigit
                if (operator == '-')
                    result -= nextDigit
            }
        }

        return result
    }

    // Handles all multiplication and division first (operator precedence)
    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        while (list.contains('x') || list.contains('/')) {
            list = calcTimesDiv(list)
        }
        return list
    }

    // Helper function to do one step of multiplication or division
    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex && i < restartIndex) {
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float
                when (operator) {
                    'x' -> {
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i + 1
                    }
                    '/' -> {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }
                    else -> {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }

            if (i > restartIndex)
                newList.add(passedList[i])
        }

        return newList
    }

    // Converts the string input into a list of numbers and operators
    private fun digitsOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""

        for (character in workingsTV.text) {
            if (character.isDigit() || character == '.') {
                currentDigit += character
            } else {
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(character)
            }
        }

        if (currentDigit != "") {
            list.add(currentDigit.toFloat())
        }

        return list
    }
}
