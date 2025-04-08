package ovh.gabrielhuav.tictactoejson

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Tag para Logcat
    private val TAG = "TicTacToe"

    // Representación del tablero de juego
    private var gameBoard = Array(3) { Array(3) { "" } }

    // Seguimiento del turno actual (true para X, false para O)
    private var isXTurn = true
    private var gameActive = true

    // Contadores de puntaje
    private var scoreX = 0
    private var scoreO = 0

    // Referencias a los elementos de la UI
    private lateinit var statusTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var resetButton: Button
    private lateinit var saveButton: Button
    private lateinit var loadButton: Button
    private lateinit var buttons: Array<Array<Button>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        statusTextView = findViewById(R.id.statusTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        resetButton = findViewById(R.id.resetButton)

        // Nuevos botones para guardar/cargar
        saveButton = findViewById(R.id.saveButton)
        loadButton = findViewById(R.id.loadButton)

        // Inicializar array de botones
        buttons = Array(3) { row ->
            Array(3) { col ->
                findViewById<Button>(
                    resources.getIdentifier(
                        "button$row$col",
                        "id",
                        packageName
                    )
                )
            }
        }

        // Configurar listener para el botón de reinicio
        resetButton.setOnClickListener {
            resetGame()
        }

        // Configurar listeners para guardar/cargar
        saveButton.setOnClickListener {
            saveGameToPreferences()
        }

        loadButton.setOnClickListener {
            showSavedGamesDialog()
        }

        // Configurar listeners para los botones del tablero
        for (i in 0..2) {
            for (j in 0..2) {
                buttons[i][j].setOnClickListener {
                    onCellClicked(i, j)
                }
            }
        }

        // Inicializar el juego
        resetGame(resetScore = false)
    }

    private fun onCellClicked(row: Int, col: Int) {
        // Verificar si la celda ya está ocupada o si el juego ya terminó
        if (gameBoard[row][col].isNotEmpty() || !gameActive) {
            return
        }

        // Marcar la celda con X u O según el turno
        val currentSymbol = if (isXTurn) "X" else "O"
        gameBoard[row][col] = currentSymbol

        // Actualizar visualmente el botón
        updateButtonAppearance(row, col)

        // Comprobar si hay un ganador o empate
        if (checkForWinner()) {
            val winner = if (isXTurn) "X" else "O"
            statusTextView.text = "¡Jugador $winner ha ganado!"

            // Actualizar puntaje
            if (isXTurn) scoreX++ else scoreO++
            updateScoreText()

            gameActive = false

            // Mostrar diálogo de victoria
            showGameOverDialog("¡Jugador $winner ha ganado!")
        } else if (isBoardFull()) {
            statusTextView.text = "¡Empate!"
            gameActive = false
            showGameOverDialog("¡Empate!")
        } else {
            // Cambiar de turno
            isXTurn = !isXTurn
            updateTurnText()
        }
    }

    private fun updateButtonAppearance(row: Int, col: Int) {
        buttons[row][col].text = gameBoard[row][col]

        // Cambiar color del texto según el símbolo
        if (gameBoard[row][col] == "X") {
            buttons[row][col].setTextColor(resources.getColor(R.color.player_x_color, null))
        } else if (gameBoard[row][col] == "O") {
            buttons[row][col].setTextColor(resources.getColor(R.color.player_o_color, null))
        }
    }

    private fun checkForWinner(): Boolean {
        // Verificar filas
        for (i in 0..2) {
            if (gameBoard[i][0].isNotEmpty() &&
                gameBoard[i][0] == gameBoard[i][1] &&
                gameBoard[i][1] == gameBoard[i][2]
            ) {
                highlightWinningCells(i, 0, i, 1, i, 2)
                return true
            }
        }

        // Verificar columnas
        for (i in 0..2) {
            if (gameBoard[0][i].isNotEmpty() &&
                gameBoard[0][i] == gameBoard[1][i] &&
                gameBoard[1][i] == gameBoard[2][i]
            ) {
                highlightWinningCells(0, i, 1, i, 2, i)
                return true
            }
        }

        // Verificar diagonal principal
        if (gameBoard[0][0].isNotEmpty() &&
            gameBoard[0][0] == gameBoard[1][1] &&
            gameBoard[1][1] == gameBoard[2][2]
        ) {
            highlightWinningCells(0, 0, 1, 1, 2, 2)
            return true
        }

        // Verificar diagonal secundaria
        if (gameBoard[0][2].isNotEmpty() &&
            gameBoard[0][2] == gameBoard[1][1] &&
            gameBoard[1][1] == gameBoard[2][0]
        ) {
            highlightWinningCells(0, 2, 1, 1, 2, 0)
            return true
        }

        return false
    }

    private fun highlightWinningCells(
        row1: Int, col1: Int,
        row2: Int, col2: Int,
        row3: Int, col3: Int
    ) {
        // Cambiar el fondo de las celdas ganadoras para destacarlas
        buttons[row1][col1].setBackgroundResource(R.drawable.winning_cell_background)
        buttons[row2][col2].setBackgroundResource(R.drawable.winning_cell_background)
        buttons[row3][col3].setBackgroundResource(R.drawable.winning_cell_background)
    }

    private fun isBoardFull(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (gameBoard[i][j].isEmpty()) {
                    return false
                }
            }
        }
        return true
    }

    private fun resetGame(resetScore: Boolean = false) {
        // Limpiar el tablero
        for (i in 0..2) {
            for (j in 0..2) {
                gameBoard[i][j] = ""
                buttons[i][j].text = ""
                buttons[i][j].setBackgroundResource(R.drawable.button_background)
            }
        }

        // Reiniciar turno
        isXTurn = true
        gameActive = true
        updateTurnText()

        // Reiniciar puntajes si se solicita
        if (resetScore) {
            scoreX = 0
            scoreO = 0
            updateScoreText()
        }
    }

    private fun updateTurnText() {
        val currentPlayer = if (isXTurn) "X" else "O"
        statusTextView.text = "¡Turno del Jugador $currentPlayer!"

        // Cambiar color del texto según el jugador
        val textColor = if (isXTurn)
            resources.getColor(R.color.player_x_color, null)
        else
            resources.getColor(R.color.player_o_color, null)

        statusTextView.setTextColor(textColor)
    }

    private fun updateScoreText() {
        scoreTextView.text = "Puntaje: X = $scoreX | O = $scoreO"
    }

    private fun showGameOverDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Fin del Juego")
            .setMessage("$message\n¿Quieres jugar otra partida?")
            .setPositiveButton("¡Sí!") { _, _ ->
                resetGame(resetScore = false)
            }
            .setNegativeButton("Reiniciar Todo") { _, _ ->
                resetGame(resetScore = true)
            }
            .setNeutralButton("Guardar Partida") { _, _ ->
                saveGameToPreferences()
                resetGame(resetScore = false)
            }
            .setCancelable(false)
            .show()
    }

    // IMPLEMENTACIÓN USANDO SHAREDPREFERENCES

    private fun saveGameToPreferences() {
        try {
            // Convertir el tablero a JSON
            val boardJson = JSONArray()
            for (i in 0..2) {
                val rowJson = JSONArray()
                for (j in 0..2) {
                    rowJson.put(gameBoard[i][j])
                }
                boardJson.put(rowJson)
            }

            // Crear JSON con todos los datos del juego
            val gameJson = JSONObject()
            gameJson.put("board", boardJson)
            gameJson.put("isXTurn", isXTurn)
            gameJson.put("scoreX", scoreX)
            gameJson.put("scoreO", scoreO)
            gameJson.put("gameActive", gameActive)
            gameJson.put("timestamp", System.currentTimeMillis())

            // Obtener SharedPreferences para las partidas guardadas
            val prefs = getSharedPreferences("saved_games", MODE_PRIVATE)

            // Generar una clave única para este guardado
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val saveKey = "game_$timestamp"

            // Guardar el juego actual
            prefs.edit()
                .putString(saveKey, gameJson.toString())
                .apply()

            // Actualizar lista de partidas guardadas
            val savedGamesList = prefs.getStringSet("saved_games_list", mutableSetOf()) ?: mutableSetOf()
            val newList = savedGamesList.toMutableSet() // Crear una copia para modificar
            newList.add(saveKey)
            prefs.edit()
                .putStringSet("saved_games_list", newList)
                .apply()

            Log.d(TAG, "Partida guardada con clave: $saveKey")
            Log.d(TAG, "Lista de partidas: $newList")
            Toast.makeText(this, "Partida guardada correctamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar partida en SharedPreferences", e)
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSavedGamesDialog() {
        try {
            // Obtener SharedPreferences
            val prefs = getSharedPreferences("saved_games", MODE_PRIVATE)

            // Obtener lista de partidas guardadas
            val savedGamesList = prefs.getStringSet("saved_games_list", setOf()) ?: setOf()

            Log.d(TAG, "Lista de partidas recuperada: $savedGamesList")

            if (savedGamesList.isEmpty()) {
                Toast.makeText(this, "No hay partidas guardadas", Toast.LENGTH_SHORT).show()
                return
            }

            // Organizar las partidas
            val savedGames = savedGamesList.mapNotNull { key ->
                val gameJsonStr = prefs.getString(key, null)
                if (gameJsonStr != null) {
                    try {
                        val gameJson = JSONObject(gameJsonStr)
                        val timestamp = gameJson.optLong("timestamp", 0L)
                        val scoreX = gameJson.optInt("scoreX", 0)
                        val scoreO = gameJson.optInt("scoreO", 0)

                        SavedGameInfo(
                            key = key,
                            timestamp = timestamp,
                            displayText = formatSavedGameDisplay(timestamp, scoreX, scoreO)
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear juego guardado: $key", e)
                        null
                    }
                } else {
                    null
                }
            }.sortedByDescending { it.timestamp }

            if (savedGames.isEmpty()) {
                Toast.makeText(this, "No hay partidas válidas guardadas", Toast.LENGTH_SHORT).show()
                return
            }

            // Mostrar diálogo con las partidas
            val items = savedGames.map { it.displayText }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Cargar Partida")
                .setItems(items) { _, which ->
                    loadGameFromPreferences(savedGames[which].key)
                }
                .setNegativeButton("Cancelar", null)
                .show()

        } catch (e: Exception) {
            Log.e(TAG, "Error al listar partidas guardadas", e)
            Toast.makeText(this, "Error al listar partidas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatSavedGameDisplay(timestamp: Long, scoreX: Int, scoreO: Int): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(timestamp))
        return "$dateStr - X: $scoreX, O: $scoreO"
    }

    private fun loadGameFromPreferences(key: String) {
        try {
            // Obtener SharedPreferences
            val prefs = getSharedPreferences("saved_games", MODE_PRIVATE)

            // Obtener datos del juego guardado
            val gameJsonStr = prefs.getString(key, null)

            if (gameJsonStr == null) {
                Toast.makeText(this, "No se pudo cargar la partida", Toast.LENGTH_SHORT).show()
                return
            }

            // Parsear JSON
            val gameJson = JSONObject(gameJsonStr)

            // Cargar tablero
            val boardJson = gameJson.getJSONArray("board")
            for (i in 0 until boardJson.length()) {
                val rowJson = boardJson.getJSONArray(i)
                for (j in 0 until rowJson.length()) {
                    gameBoard[i][j] = rowJson.getString(j)
                }
            }

            // Cargar otros datos
            isXTurn = gameJson.getBoolean("isXTurn")
            scoreX = gameJson.getInt("scoreX")
            scoreO = gameJson.getInt("scoreO")
            gameActive = gameJson.optBoolean("gameActive", true)

            // Actualizar UI
            for (i in 0..2) {
                for (j in 0..2) {
                    updateButtonAppearance(i, j)
                }
            }

            updateTurnText()
            updateScoreText()

            // Verificar si el juego estaba en un estado terminal
            if (!gameActive || checkForWinner() || isBoardFull()) {
                gameActive = false
                if (checkForWinner()) {
                    val winner = if (isXTurn) "X" else "O"
                    statusTextView.text = "¡Jugador $winner ha ganado!"
                } else if (isBoardFull()) {
                    statusTextView.text = "¡Empate!"
                }
            }

            Log.d(TAG, "Partida cargada correctamente: $key")
            Toast.makeText(this, "Partida cargada correctamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar partida desde SharedPreferences", e)
            Toast.makeText(this, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Clase interna para manejar información de partidas guardadas
    data class SavedGameInfo(
        val key: String,
        val timestamp: Long,
        val displayText: String
    )
}