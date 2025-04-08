package ovh.gabrielhuav.tictactoejson

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Clase que representa el estado del juego para ser guardado/cargado
 */
data class GameState(
    val gameBoard: Array<Array<String>>,
    val isXTurn: Boolean,
    val scoreX: Int,
    val scoreO: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val singlePlayerMode: Boolean,
    val computerDifficulty: Int
) {
    // Función para convertir el estado del juego a un objeto JSON
    fun toJson(): JSONObject {
        val json = JSONObject()

        // Convertir el tablero a JSON
        val boardJson = JSONArray()
        for (i in gameBoard.indices) {
            val rowJson = JSONArray()
            for (j in gameBoard[i].indices) {
                rowJson.put(gameBoard[i][j])
            }
            boardJson.put(rowJson)
        }

        json.put("gameBoard", boardJson)
        json.put("isXTurn", isXTurn)
        json.put("scoreX", scoreX)
        json.put("scoreO", scoreO)
        json.put("timestamp", timestamp)
        json.put("singlePlayerMode", singlePlayerMode)
        json.put("computerDifficulty", computerDifficulty)

        return json
    }

    // Guardar el estado a un archivo
    fun saveToFile(context: Context, filename: String? = null): String {
        val actualFilename = filename ?: generateFilename()
        val file = File(context.filesDir, actualFilename)
        file.writeText(toJson().toString())
        return actualFilename
    }

    private fun generateFilename(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(timestamp))
        return "tictactoe_$dateStr.json"
    }

    companion object {
        // Parsear un objeto JSON a GameState
        fun fromJson(json: JSONObject): GameState {
            // Obtener el tablero desde JSON
            val boardJson = json.getJSONArray("gameBoard")
            val gameBoard = Array(3) { Array(3) { "" } }

            for (i in 0 until boardJson.length()) {
                val rowJson = boardJson.getJSONArray(i)
                for (j in 0 until rowJson.length()) {
                    gameBoard[i][j] = rowJson.getString(j)
                }
            }

            return GameState(
                gameBoard = gameBoard,
                isXTurn = json.getBoolean("isXTurn"),
                scoreX = json.getInt("scoreX"),
                scoreO = json.getInt("scoreO"),
                timestamp = json.getLong("timestamp"),
                singlePlayerMode = json.getBoolean("singlePlayerMode"),
                computerDifficulty = json.getInt("computerDifficulty")
            )
        }

        // Cargar el estado desde un archivo
        fun loadFromFile(context: Context, filename: String): GameState {
            val file = File(context.filesDir, filename)
            val jsonStr = file.readText()
            return fromJson(JSONObject(jsonStr))
        }

        // Listar todos los archivos de guardado disponibles
        fun listSavedGames(context: Context): List<SavedGameInfo> {
            val directory = context.filesDir
            val jsonFiles = directory.listFiles { file ->
                file.name.startsWith("tictactoe_") && file.name.endsWith(".json")
            }

            return jsonFiles?.map { file ->
                try {
                    val jsonStr = file.readText()
                    val json = JSONObject(jsonStr)

                    val timestamp = json.getLong("timestamp")
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(timestamp))

                    val scoreX = json.getInt("scoreX")
                    val scoreO = json.getInt("scoreO")

                    SavedGameInfo(
                        filename = file.name,
                        date = dateStr,
                        description = "X: $scoreX - O: $scoreO"
                    )
                } catch (e: Exception) {
                    SavedGameInfo(
                        filename = file.name,
                        date = "Desconocido",
                        description = "Error al cargar información"
                    )
                }
            } ?: emptyList()
        }
    }

    // Sobreescribimos equals y hashCode para comparación correcta
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!gameBoard.contentDeepEquals(other.gameBoard)) return false
        if (isXTurn != other.isXTurn) return false
        if (scoreX != other.scoreX) return false
        if (scoreO != other.scoreO) return false
        if (singlePlayerMode != other.singlePlayerMode) return false
        if (computerDifficulty != other.computerDifficulty) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameBoard.contentDeepHashCode()
        result = 31 * result + isXTurn.hashCode()
        result = 31 * result + scoreX
        result = 31 * result + scoreO
        result = 31 * result + singlePlayerMode.hashCode()
        result = 31 * result + computerDifficulty
        return result
    }
}

// Clase para mostrar información de partidas guardadas en la lista
data class SavedGameInfo(
    val filename: String,
    val date: String,
    val description: String
)