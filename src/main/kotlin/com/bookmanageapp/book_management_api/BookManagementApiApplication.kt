package com.bookmanageapp.book_management_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookManagementApiApplication

fun main(args: Array<String>) {
	runApplication<BookManagementApiApplication>(*args)
}
