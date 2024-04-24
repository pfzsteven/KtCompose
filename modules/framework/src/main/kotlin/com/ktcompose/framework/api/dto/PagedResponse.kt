package com.ktcompose.framework.api.dto

/**
 * 分页型数据结构
 */
class PagedResponse<T> {
    /**
     * 是否有下一页
     */
    var hasNextPage: Boolean = false

    /**
     * 当前分页序号
     */
    var pageNum: Int = 0

    /**
     * 下一分页序号
     */
    var nextPage: Int = 0

    /**
     * 总页数
     */
    var total: Int = 0

    /**
     * 当前分页数据
     */
    val list: ArrayList<T> = ArrayList()
}