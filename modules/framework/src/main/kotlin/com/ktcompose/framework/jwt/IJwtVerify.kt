package com.ktcompose.framework.jwt

/**
 * JWT鉴权接口
 */
interface IJwtVerify {

    /**
     * 生成jwt时，允许扩展添加参数进行生成jwt字符串
     */
    suspend fun buildPayload(map: HashMap<String, String>)

    /**
     * 二次验证(在执行该函数之前，已对"是否已过期" 验证通过。若已过期，不会执行该函数。)
     * @param path url path
     * @param authentication 完整jwt信息
     * @param payload: authentication解码后的payload部分
     */
    suspend fun verify(path: String, authentication: String, payload: JwtPayload): Boolean = true
}