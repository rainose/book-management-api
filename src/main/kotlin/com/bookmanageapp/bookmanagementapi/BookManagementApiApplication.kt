package com.bookmanageapp.bookmanagementapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 書籍管理APIアプリケーションのエントリーポイント。
 *
 * @author nose yudai
 */
@SpringBootApplication
class BookManagementApiApplication

/**
 * アプリケーションを起動するメイン関数。
 *
 * @param args コマンドライン引数
 * @author nose yudai
 */
fun main(args: Array<String>) {
    runApplication<BookManagementApiApplication>(*args)
}
