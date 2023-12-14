package com.example.recyclerview.model;

data class User (
    val id: Long, // Уникальный номер пользователя
    val name: String, // Имя человека
    val company: String, // Название комании
    val photo: String, // Ссылка на фото человека
    val isLiked: Boolean // Был ли лайкнут пользователь
)
