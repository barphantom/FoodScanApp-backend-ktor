package com.example

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction


object Products : IntIdTable() {
    val barcode = varchar("barcode", 20).uniqueIndex()
    val name = varchar("name", 20)
    val calories = double("calories")
    val protein = double("protein")
    val fat = double("fat")
    val carbs = double("carbs")
    val weight = double("weight")
}

object IngredientsTable : IntIdTable() {
    val product = reference("product_id", Products)
    val name = varchar("name", 255)
    val tag = varchar("tag", 20)
}

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:myapp.db", driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(Products, IngredientsTable)
        }
    }
}