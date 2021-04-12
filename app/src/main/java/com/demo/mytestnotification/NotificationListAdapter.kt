package com.demo.mytestnotification

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.demo.mytestnotification.Utils.CHANNEL_ID_1

class CustomAdapter(dataListMy: ArrayList<MyNotificationData>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    var dataSetMy: ArrayList<MyNotificationData> = arrayListOf<MyNotificationData>()
    init {
        dataSetMy.addAll(dataListMy)
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
        val title: TextView = view.findViewById(R.id.notif_title)
        val body: TextView = view.findViewById(R.id.notif_body)
        val time: TextView = view.findViewById(R.id.notif_time)
        val oldestTxt: TextView = view.findViewById(R.id.oldest_float_txt)

    }

    fun updateList(newDataSet: ArrayList<MyNotificationData>) {
        val diffCallback = MyDiffCallback(dataSetMy, newDataSet)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataSetMy.clear()
        dataSetMy.addAll(newDataSet)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateLis_oldt(data: ArrayList<MyNotificationData>) {
        //android.util.Log.i("+++", "+++ entr updateList(${data.size})")
        dataSetMy.clear()
        notifyDataSetChanged()
        dataSetMy.addAll(data)
        notifyDataSetChanged()
        //android.util.Log.i("+++", "+++ --- exit updateList(${data.size})")
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.notification_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val notifItem = dataSetMy[position]
        viewHolder.id.text = Html.fromHtml("<b>id: ${notifItem.id.toString()}</b>")
        viewHolder.grp_channel_id.text = ", [group: ${notifItem.groupId}, channelId: ${notifItem.channelId}]"
        viewHolder.title.text = notifItem.title
        viewHolder.body.text = notifItem.body
        viewHolder.time.text = "post at:"+ notifItem.time.toString()
        viewHolder.oldestTxt.visibility = if (notifItem.oldest) View.VISIBLE else View.GONE

        updateChannleBk(viewHolder, notifItem)

        //viewHolder.img


        //android.util.Log.v("+++", "+++ onBindViewHolder($position, ${dataSet[position].toString()})")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSetMy.size

    fun updateChannleBk(viewHolder: ViewHolder, notiItem: MyNotificationData) {
        notiItem.channelId?.let {
            val cl = when (it) {
                CHANNEL_ID_1 -> ContextCompat.getColor(viewHolder.itemView.context, R.color.channel_1_color)
                else -> ContextCompat.getColor(viewHolder.itemView.context, R.color.channel_1_color)
            }
            viewHolder.row_ll.setBackgroundColor(cl)
        }
    }
}
