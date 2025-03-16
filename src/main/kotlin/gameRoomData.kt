package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

object DatabaseFactory {
    fun init() {
        val url = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/game_room"
        val driver = "org.postgresql.Driver"
        val user = System.getenv("DB_USER") ?: ""
        val password = System.getenv("DB_PASSWORD") ?: ""

        Database.connect(url, driver, user, password)
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

fun main(args: Array<String>): Unit {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080 // Get from env or default to 8080
    println("Starting server on port: $port")
    EngineMain.main(args + arrayOf("-port=$port")) // Dynamically pass -port argument
}