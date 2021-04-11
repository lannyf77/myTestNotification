package com.demo.mytestnotification

import com.demo.mytestnotification.Utils.CHANNEL_ID_1

data class MyNotificationData (val id: Int, val title: String, val body: String, val time: Long, val channelId: String?=CHANNEL_ID_1, val groupId: String?=null, val oldest: Boolean=false) {
    override fun toString(): String {
        return "id=$id, $title, $body, $time, [channelId: $channelId, groupId: $groupId]"
    }
}
