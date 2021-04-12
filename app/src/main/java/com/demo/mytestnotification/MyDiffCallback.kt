package com.demo.mytestnotification

import androidx.recyclerview.widget.DiffUtil


internal class MyDiffCallback(private val oldNotifys: ArrayList<MyNotificationData>, private val newNoifys: ArrayList<MyNotificationData>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldNotifys.size
    }

    override fun getNewListSize(): Int {
        return newNoifys.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldNotifys[oldItemPosition].equals(newNoifys[newItemPosition])
        //return oldNotifys[oldItemPosition].id === newNoifys[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldNotifys[oldItemPosition].equals(newNoifys[newItemPosition])
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}

