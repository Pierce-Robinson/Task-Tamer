package com.varsitycollege.tasktamer.data

data class Category(
    val id: String ?= null,
    val title: String ?= null,
    val description: String ?= null,
    val client: String ?= null,
    val deadline: String ?= null,
    val colour: String ?= null,
    val userId: String ?= null
    //val tasks: List<String>
)
{
    override fun toString(): String {
        return "Category title: $title"
    }
}
