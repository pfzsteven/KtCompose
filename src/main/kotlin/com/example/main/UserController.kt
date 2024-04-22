package com.example.main

import com.ktcompose.framework.api.dto.ApiResponse
import com.ktcompose.framework.http.HttpRequest
import com.example.main.dto.UserDTO

object UserController {

    suspend fun getUserInfo(request: HttpRequest): ApiResponse<UserDTO> {
        println("==== UserController:token is:[${request.header.getAuthorization()}]")
        return ApiResponse.success(UserDTO("111", "John"))
    }
}