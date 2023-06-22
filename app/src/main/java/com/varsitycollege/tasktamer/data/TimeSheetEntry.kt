package com.varsitycollege.tasktamer.data

data class TimeSheetEntry(
    val id: String ?= null,
    val description: String ?= null,
    val date: String ?= null,
    val startTime: String ?= null,
    val endTime: String ?= null,
    val billable: Boolean ?= null,
    var rate: Double ?= null,
    val imageId: String ?= null,
    val colour: String ?= null,
    val categoryId: String ?= null,
    val userId: String ?= null
)

//Check slide 23 json
