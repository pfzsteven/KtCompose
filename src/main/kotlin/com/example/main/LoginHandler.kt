package com.example.main

import com.example.main.dto.UserDTO
import com.ktcompose.framework.api.dto.ApiResponse
import com.ktcompose.framework.http.HttpRequest
import com.ktcompose.framework.jwt.JwtManager
import com.ktcompose.framework.role.Role


object LoginHandler {

    /**
     * 登录v1业务处理
     */
    suspend fun loginV1(request: HttpRequest, email: String?, code: String?): ApiResponse<UserDTO> {
        println("====> loginV1 email:$email  code:$code")
        val jwtToken = JwtManager.generateJwt(Role.User) { map ->
            map["uid"] = "111"
        }
        return ApiResponse.success(UserDTO("111", "John")).addHead(JwtManager.httpHeaderName(), jwtToken)
    }

    /**
     * 登录v2业务处理
     */
    suspend fun loginV2(email: String?, code: String?, mobile: String?): ApiResponse<UserDTO> {
        println("====> loginV2 email:$email  code:$code  mobile: $mobile")
        val jwtToken = JwtManager.generateJwt(Role.User) { map ->
            map["uid"] = "222"
        }
        return ApiResponse.success(UserDTO("222", "John")).addHead(JwtManager.httpHeaderName(), jwtToken)
    }
}