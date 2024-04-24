package com.ktcompose.framework.role

enum class Role(val level: Int) {
    Admin(100), Developer(90), Operator(80), User(70), Guest(0)
}