package ca.gbc.restaurantguide.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.restaurantguide.R
import ca.gbc.restaurantguide.data.Restaurant

class RestaurantAdapter(
    private val onClick: (Restaurant) -> Unit,
    private val onEdit: (Restaurant) -> Unit
) : ListAdapter<Restaurant, RestaurantAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Restaurant>() {
            override fun areItemsTheSame(o: Restaurant, n: Restaurant) = o.id == n.id
            override fun areContentsTheSame(o: Restaurant, n: Restaurant) = o == n
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val rating: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)

        fun bind(item: Restaurant) {
            tvName.text = item.name
            tvAddress.text = item.address
            rating.rating = item.rating.toFloat()

            itemView.setOnClickListener { onClick(item) }
            btnEdit.setOnClickListener { onEdit(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    fun getItemAt(position: Int): Restaurant = getItem(position)
}
