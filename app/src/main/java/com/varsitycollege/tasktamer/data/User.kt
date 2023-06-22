package com.varsitycollege.tasktamer.data

data class User (
    val id: String ?= null,
    val displayName: String ?= null,
    var darkMode: Boolean ?= null,
    var minHours: Double ?= null,
    var maxHours: Double ?= null
)