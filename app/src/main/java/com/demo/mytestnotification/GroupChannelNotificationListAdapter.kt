package com.demo.mytestnotification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo.mytestnotification.Utils.CHANNEL_ID_1
import com.demo.mytestnotification.Utils.CHANNEL_ID_2
import com.demo.mytestnotification.Utils.CHANNEL_ID_3
import com.demo.mytestnotification.Utils.CHANNEL_ID_4

class GroupChannelAdapter(dataList: ArrayList<NotificationData>,
    val groupId: String, val channelId: String) : RecyclerView.Adapter<GroupChannelAdapter.ViewHolder>() {

    var dataSet: ArrayList<NotificationData> = arrayListOf<NotificationData>()
    init {
        dataSet.addAll(dataList)
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val row_ll = view.findViewById<CardView>(R.id.row_ll)
        val img: ImageView = view.findViewById(R.id.channelid)
        val id: TextView = view.findViewById(R.id.notif_id)
        val grp_channel_id: TextView = view.findViewById(R.id.notif_grp_channel_id)
        val content: TextView = view.findViewById(R.id.notif_content)
        val oldestTxt: TextView = view.findViewById(R.id.oldest_float_txt)
    }

    fun updateList(data: ArrayList<NotificationData>) {
        android.util.Log.i("+++", "+++ enter updateList(${data.size}), $groupId, $channelId")
        dataSet.clear()
        notifyDataSetChanged()
        dataSet.addAll(data)
        notifyDataSetChanged()
        android.util.Log.i("+++", "+++ --- exit updateList(${data.size})")
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.group_channel_notification_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val notifItem = dataSet[position]
        viewHolder.id.text = "id: "+notifItem.id.toString()
        viewHolder.grp_channel_id.text = ", [${notifItem.time}]"
        viewHolder.content.text = "${notifItem.title}, ${notifItem.body}"

        viewHolder.oldestTxt.visibility = if (notifItem.oldest) View.VISIBLE else View.GONE

        updateChannleBk(viewHolder, notifItem)

        //viewHolder.img


        android.util.Log.v("+++", "+++ onBindViewHolder($position, ${dataSet[position].toString()}), $groupId, $channelId")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
        .also {
            android.util.Log.v("+++", "+++ getItemCount() ret: $it, $groupId, $channelId")
        }

    fun updateChannleBk(viewHolder: ViewHolder, notiItem: NotificationData) {
        notiItem.channelId?.let {
            val cl = when (it) {
                CHANNEL_ID_1 -> {
                    Pair(ContextCompat.getColor(viewHolder.itemView.context, R.color.channel_1_color), R.drawable.ic_one)
                }
                CHANNEL_ID_2 -> {
                    Pair(ContextCompat.getColor(viewHolder.itemView.context, R.color.channel_2_color), R.drawable.ic_two)
                }
                CHANNEL_ID_3 -> {
                    Pair(ContextCompat.getColor(viewHolder.itemView.context, R.color.channel_3_color), R.drawable.ic_three)
                }
                CHANNEL_ID_4 -> {
                    Pair(ContextCompat.getColor(viewHolder.itemView.context, R.color.channel_4_color), R.drawable.ic_four)
                }
                else -> {
                    Pair(ContextCompat.getColor(viewHolder.itemView.context, R.color.channel_1_color), R.drawable.ic_one)
                }
            }
            viewHolder.row_ll.setBackgroundColor(cl.first)
            viewHolder.img.setImageResource(cl.second)
        }
    }
}
