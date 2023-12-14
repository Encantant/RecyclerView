package com.example.recyclerview

import android.app.Application
import com.example.recyclerview.model.UsersService

class App : Application() {

    val usersService = UsersService()
}