package com.example

import java.util.*
import kotlin.math.roundToInt


data class Fruit (
    var point: Point
) {
    fun placeApple() {
        val random = Random()

        var y = (random.nextInt(HEIGHT/ BLOCK_SIZE).toDouble() / BLOCK_SIZE).roundToInt() * BLOCK_SIZE
        var x = (random.nextInt(WIDTH/ BLOCK_SIZE).toDouble() / BLOCK_SIZE).roundToInt() * BLOCK_SIZE

        if (y >= HEIGHT) {
            y -= BLOCK_SIZE
        }
        if (x >= WIDTH) {
            x -= BLOCK_SIZE
        }
        point = Point(x, y)

    }

}



fun Fruit.touchFood(snake: Snake) = snake.head == point
