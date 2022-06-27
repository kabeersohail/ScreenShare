package com.example.screenshare.results

open class DataResult<Any>{
    var successful: Boolean = false
    var result: String? = null
    var message: String? = null
    var statusCode: Int = 0
    var entity: Any? = null
    var entities: List<Any> = ArrayList()
    var UUID: String? = null
}