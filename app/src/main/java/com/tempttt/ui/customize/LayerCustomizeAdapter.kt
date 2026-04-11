package com.tempttt.ui.customize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tempttt.R
import com.tempttt.core.extensions.gone
import com.tempttt.core.extensions.tap
import com.tempttt.core.extensions.visible
import com.tempttt.core.utils.DataLocal
import com.tempttt.core.utils.key.AssetsKey
import com.tempttt.data.model.custom.ItemNavCustomModel
import com.tempttt.databinding.ItemCustomizeBinding
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerDrawable

class LayerCustomizeAdapter(val context: Context) : ListAdapter<ItemNavCustomModel, LayerCustomizeAdapter.CustomizeViewHolder>(DiffCallback) {

    var onItemClick: ((ItemNavCustomModel, Int) -> Unit) = { _, _ -> }
    var onNoneClick: ((Int) -> Unit) = {}
    var onRandomClick: (() -> Unit) = {}

    inner class CustomizeViewHolder(val binding: ItemCustomizeBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: ItemNavCustomModel, position: Int) {
            binding.apply {
                val shimmerDrawable = ShimmerDrawable().apply {
                    setShimmer(DataLocal.shimmer)
                    startShimmer()
                }

                val itemType = when (item.path) {
                    AssetsKey.NONE_LAYER -> "NONE"
                    AssetsKey.RANDOM_LAYER -> "RANDOM"
                    else -> "IMAGE"
                }
                android.util.Log.d(
                    "ItemCustomizeState",
                    "[$position] type=$itemType | isSelected=${item.isSelected} | path=${item.path} | colors=${item.listImageColor.size}"
                )

                if (item.isSelected) {
                    // Bring selected item to front with elevation
                    root.translationZ = 16f
                    root.scaleX = 1.0f
                    root.scaleY = 1.0f
                    vFocus.gone()

                    cardLayerItem.setBackgroundResource(R.drawable.layer_slt)
                    // vFocus.setBackgroundResource(R.drawable.bg_10_stroke_yellow)
                } else {
                    // Reset to normal state
                    root.translationZ = 0f
                    root.scaleX = 1f
                    root.scaleY = 1f
                    vFocus.gone()
                    cardLayerItem.setBackgroundResource(R.drawable.layer_uslt)


                }

                when (item.path) {
                    AssetsKey.NONE_LAYER -> {
                        btnNone.visible()
                        btnRandom.gone()
                        imvImage.gone()
                    }
                    AssetsKey.RANDOM_LAYER -> {
                        btnNone.gone()
                        btnRandom.visible()
                        imvImage.gone()
                    }
                    else -> {
                        btnNone.gone()
                        imvImage.visible()
                        btnRandom.gone()
                        val cornerRadiusPx = (8f * context.resources.displayMetrics.density).toInt()
                        Glide.with(root).load(item.thumb.ifEmpty { item.path }).placeholder(shimmerDrawable).transform(RoundedCorners(cornerRadiusPx)).into(imvImage)
                    }
                }

                binding.imvImage.tap(100) { onItemClick.invoke(item, position) }

                binding.btnRandom.tap { onRandomClick.invoke() }

                binding.btnNone.tap { onNoneClick.invoke(position) }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizeViewHolder {
        return CustomizeViewHolder(ItemCustomizeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CustomizeViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<ItemNavCustomModel>(){
            override fun areItemsTheSame(oldItem: ItemNavCustomModel, newItem: ItemNavCustomModel): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: ItemNavCustomModel, newItem: ItemNavCustomModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}