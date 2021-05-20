package com.example

class Snake() {

    var isDead = false
    val tail = mutableListOf<Point>()
    val head by lazy { tail[0] }

    init {
        tail.add(Point(4, 4))
        tail.add(Point(4, 5))

    }

    var dir: Direction = Direction.RIGHT

    fun addTailPart() = tail.add(Point(tail[tail.size - 1].x, tail[tail.size - 1].y))

    fun isCollideTail(): Boolean {
        tail.forEachIndexed { index, part ->
            if (index > 0 && part.x == head.x && part.y == head.y) {
                return true
            }
        }

        return false
    }


    fun snakeUpdate() {
        for (i in tail.size - 1 downTo 1) {
            val current = tail[i]
            val (x, y) = tail[i - 1]
            current.x = x
            current.y = y
        }
        when (dir) {
            Direction.UP -> {
                head.y = head.y - 1
                if (head.y < 0) {
                    isDead = true
                }
            }
            Direction.RIGHT -> {
                head.x += 1
                if (head.x >= WIDTH / BLOCK_SIZE) {
                    isDead = true
                }
            }
            Direction.LEFT -> {
                head.x -= 1
                if (head.x < 0) {
                    isDead = true
                }
            }
            Direction.DOWN -> {
                head.y += 1
                if (head.y >= HEIGHT / BLOCK_SIZE) {
                    isDead = true
                }
            }
        }
    }
}