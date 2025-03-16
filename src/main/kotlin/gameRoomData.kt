package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable

object DatabaseFactory {
    fun init() {
        val url = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/game_room"
        val driver = "org.postgresql.Driver"
        val user = System.getenv("DB_USER") ?: ""
        val password = System.getenv("DB_PASSWORD") ?: ""

        println("Connecting to database init...")
        println("JDBC_DATABASE-URL: $url")
        println("DB_USER: $user")

        try {
            Database.connect(url, driver, user, password)
            println("Connection successful!")
        }catch (e: Exception) {
            println("Connection failed: ${e.message}")
            e.printStackTrace()
        }
    }
}

object CRRooms : Table() {
    val id = integer("id").autoIncrement()
    val crRoomId = text("crRoomId")
    val createdAt = date("createdAt")
    val alertType = text("alertType").default("none")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class CreateRoomRequest(
    val crRoomId: String
)

fun Application.module() {
    install(ContentNegotiation) {json()}
    routing {
        get("/") {
            call.respondText("Server is running on port!", ContentType.Text.Plain)
        }

        post("/createRoom") {
            val request = call.receive<CreateRoomRequest>()

            try {
                transaction {
                    CRRooms.insert {
                        it[crRoomId] = request.crRoomId
                        it[createdAt] = LocalDateTime.now().toLocalDate()
                        it[alertType] = "none"
                    }
                }

                call.respond(HttpStatusCode.Created, "Room Created Successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }
    }
}

fun main() {
    // Retrieve the port from Railway's environment variable or default to 8080
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    println("Starting server on port: $port") // Debug log to verify port

    // Initialize the database connection
    println("Initializing database connection..")
    DatabaseFactory.init()

    // Start the embedded Netty server with the dynamically configured port
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}