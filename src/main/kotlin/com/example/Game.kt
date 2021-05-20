package com.example

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import java.io.File
import kotlin.system.exitProcess


class Game : Application() {

    enum class GameState {
        START, RUNNING, PAUSE, GAMEOVER
    }

    private lateinit var mainScene: Scene

    private lateinit var graphicsContext: GraphicsContext

    private lateinit var timer: AnimationTimer

    private var snake: Snake = Snake()

    private val fruit: Fruit = Fruit(Point(5, 5))

    private val users: MutableList<User> = mutableListOf()

    private var name: String = "UnknowPlayer"

    private var score: Int = 0

    private var firstGame = true

    private lateinit var gc: Image

    private var state: GameState = GameState.START

    private val currentlyActiveKeys = mutableSetOf<KeyCode>()

    override fun start(mainStage: Stage) {
        mainStage.title = "Snake"


        val canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())

        val textArea = TextArea().apply {
            maxHeight = BUTTON_HEIGHT
            maxWidth = BUTTON_WIDTH
        }

        val button = Button().apply {
            text = "New Game"
            minHeight = BUTTON_HEIGHT
            minWidth = BUTTON_WIDTH
            setOnAction {
                isVisible = false
                name = textArea.text
                textArea.isVisible = false
                state = GameState.RUNNING
                timer.start()
            }
        }

        val vBox = VBox().apply {
            prefWidthProperty().bind(canvas.widthProperty())
            prefHeightProperty().bind(canvas.heightProperty())
            alignment = Pos.CENTER
            children.addAll(textArea, button)
        }
        if (firstGame) {
            users.addAll(load())
            firstGame = false
        }

        val root = Group()
        mainScene = Scene(root)
        mainStage.scene = mainScene


        root.children.addAll(canvas, vBox)

        prepareActionHandlers()
        loadGraphics()

        graphicsContext = canvas.graphicsContext2D


        // Main loop
        timer = object : AnimationTimer() {
            var lastTick: Long = 0
            override fun handle(currentNanoTime: Long) {
                if (currentNanoTime - lastTick > 1000000000 / 5) {
                    lastTick = currentNanoTime
                    tickAndRender()
                }
                if (state == GameState.GAMEOVER) {
                    users.add(User(listOf(name, score.toString())))
                    showGameOver(graphicsContext, users)
                    timer.stop()
                    snake = Snake()
                    score = 0
                    button.isVisible = true
                    textArea.isVisible = true
                }
            }
        }
        timer.start()
        mainStage.show()

        mainStage.onCloseRequest = EventHandler {

            save()
            Platform.exit()
            exitProcess(0)
        }
    }

    private fun prepareActionHandlers() {
        mainScene.onKeyPressed = EventHandler { event ->
            currentlyActiveKeys.add(event.code)
        }
        mainScene.onKeyReleased = EventHandler { event ->
            currentlyActiveKeys.remove(event.code)
        }

    }

    private fun loadGraphics() {
        gc = Image(getResource("/grass.jpg"))

    }


    private fun tickAndRender() {
        // the time elapsed since the last frame, in nanoseconds
        // can be used for physics calculation, etc

        when (state) {
            GameState.START -> {
                showWelcomeSnake(graphicsContext)
                graphicsContext.drawImage(gc, 0.0, 0.0)
            }
            GameState.RUNNING -> {
                if (snake.isDead || snake.isCollideTail()) {
                    state = GameState.GAMEOVER
                    showGameOver(graphicsContext, users)
                }

                graphicsContext.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())
                updateSnakePosition()
                snake.snakeUpdate()

                drawFruit()
                drawSnake()


                if (fruit.touchFood(snake)) {
                    snake.addTailPart()
                    score += 10
                    fruit.placeApple()
                }
                graphicsContext.apply {
                    font = Font("", 20.0)
                    fill = Color.BLUE
                    fillText("Score: $score", 10.0, 20.0)
                }
            }
            GameState.PAUSE -> {
                showPauseScreen(graphicsContext)
                if (currentlyActiveKeys.contains(KeyCode.SPACE)) {
                    state = GameState.RUNNING
                }
            }

        }
    }

    private fun updateSnakePosition() {
        if (currentlyActiveKeys.contains(KeyCode.A)) {
            if (snake.dir != Direction.RIGHT) {
                snake.dir = Direction.LEFT
            }

        }
        if (currentlyActiveKeys.contains(KeyCode.D)) {
            if (snake.dir != Direction.LEFT) {
                snake.dir = Direction.RIGHT
            }

        }
        if (currentlyActiveKeys.contains(KeyCode.W)) {
            if (snake.dir != Direction.DOWN) {
                snake.dir = Direction.UP
            }

        }
        if (currentlyActiveKeys.contains(KeyCode.S)) {
            if (snake.dir != Direction.UP) {
                snake.dir = Direction.DOWN
            }
        }
        if (currentlyActiveKeys.contains(KeyCode.ESCAPE)) {
            state = GameState.PAUSE

        }


    }

    private fun drawSnake() {
        graphicsContext.fill = SNAKE_COLOR
        snake.tail.forEach {

            graphicsContext.fillRect(
                it.x.toDouble() * BLOCK_SIZE,
                it.y.toDouble() * BLOCK_SIZE,
                BLOCK_SIZE.toDouble(),
                BLOCK_SIZE.toDouble()
            )
        }
    }

    private fun drawFruit() {
        graphicsContext.fill = FOOD_COLOR
        graphicsContext.fillRect(
            fruit.point.x.toDouble() * BLOCK_SIZE,
            fruit.point.y.toDouble() * BLOCK_SIZE,
            BLOCK_SIZE.toDouble(),
            BLOCK_SIZE.toDouble()
        )
    }

    private fun save() {
        File("data/score.txt").printWriter().use { out ->
            users.forEach {
                out.println("${it.name},${it.score}")
            }
        }
    }

    private fun load(): List<User> {
        val allLines = File("data/score.txt").readLines()
        return allLines.map { User(it.split(",")) }
    }

    private fun showGameOver(graphics: GraphicsContext, users: List<User>) {
        graphics.apply {
            fill = Color.RED
            font = Font("", 50.0)
            fillText("GAME OVER", 100.0, 150.0)
        }

        graphics.apply {
            var x = 20
            fill = Color.BLACK
            font = Font("", 15.0)
            fillText("TOP 3 PLAYER: ", 100.0, 310.0)
            val tmp = users.sortedByDescending { it.score }.take(3)
            for (player in tmp) {
                fillText("${player.name}: ${player.score}", 110.0, 310.0 + x)
                x += 20
            }
        }
    }


    private fun showWelcomeSnake(graphics: GraphicsContext) {
        graphics.apply {
            fill = Color.PURPLE
            font = Font("", 50.0)
            fillText("Snake", 195.0, 150.0)
        }
    }

    private fun showPauseScreen(graphics: GraphicsContext) {
        graphics.apply {
            fill = Color.BLUE
            font = Font("", 20.0)
            fillText("Press space to continue...", 100.0, 250.0)
        }
    }


}





