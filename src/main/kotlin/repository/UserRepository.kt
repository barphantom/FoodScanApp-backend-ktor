package com.example.repository

import com.example.Users
import com.example.model.User
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class UserRepository {
    fun findByUid(uid: String): User? {
        return transaction {
            Users.selectAll().where { Users.uid eq uid }
                .map {
                    User(
                        id = it[Users.id].value,
                        uid = it[Users.uid]
                    )
                }
                .singleOrNull()
        }
    }

    fun createUser(uid: String): User {
        return transaction {
            val userId = Users.insertAndGetId {
                it[Users.uid] = uid
            }.value

            User(
                id = userId,
                uid = uid
            )
        }
    }
}