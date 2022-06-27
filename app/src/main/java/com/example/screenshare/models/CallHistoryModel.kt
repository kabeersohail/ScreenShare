package com.example.screenshare.models

import java.text.SimpleDateFormat

class CallHistoryModel : Comparable<CallHistoryModel> {

    var callId: String? = null
    var duration: Int? = -1
    var status: String? = null
    var displayName: String? = null
    var source: Int? = null
    var deviceId: String? = null
    var activationCode: String? = null
    var initiatedBy: String? = null
    var createdAt :String? = null
    var updatedAt :String? = null
    var type:String? = null


    override fun compareTo(callHistoryModel: CallHistoryModel): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val createdDate = sdf.parse(updatedAt)
        val compareDate= sdf.parse(callHistoryModel.updatedAt!!)
        return createdDate.compareTo(compareDate)
    }

}