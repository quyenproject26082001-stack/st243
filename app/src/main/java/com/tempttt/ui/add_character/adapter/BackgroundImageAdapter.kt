package com.tempttt.ui.add_character.adapter

import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.tempttt.core.base.BaseAdapter
import com.tempttt.R
import com.tempttt.core.extensions.gone
import com.tempttt.core.extensions.loadImage
import com.tempttt.core.extensions.select
import com.tempttt.core.extensions.tap
import com.tempttt.core.extensions.visible
import com.tempttt.data.model.SelectedModel
import com.tempttt.databinding.ItemBackgroundImageBinding

class BackgroundImageAdapter :
    BaseAdapter<SelectedModel, ItemBackgroundImageBinding>(ItemBackgroundImageBinding::inflate) {
    var onAddImageClick: (() -> Unit) = {}
    var onBackgroundImageClick: ((String, Int) -> Unit) = { _, _ -> }
    var currentSelected = -1

    override fun onBind(binding: ItemBackgroundImageBinding, item: SelectedModel, position: Int) {
        binding.apply {
            vFocus.visible()
            if (item.isSelected) {
                vFocus.setBackgroundResource(R.drawable.bg_stroke_img)
            } else {
                vFocus.setBackgroundResource(R.drawable.bg_stroke_img_uslt)
            }
            if (position == 0) {
                lnlAddItem.visible()
                imvImage.gone()
                lnlAddItem.tap(800) { onAddImageClick.invoke() }
            } else {
                lnlAddItem.gone()
                imvImage.visible()
                // Load image with 8dp rounded corners
                val cornerRadiusPx = (8 * root.context.resources.displayMetrics.density).toInt()
                Glide.with(root)
                    .load(item.path)
                    .override(256, 256)
                    .encodeQuality(60)
                    .transform(RoundedCorners(cornerRadiusPx))
                    .into(imvImage)
                imvImage.tap { onBackgroundImageClick.invoke(item.path, position) }
            }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>) {
        if (position != currentSelected) {
            items.clear()
            items.addAll(list)

            notifyItemChanged(currentSelected)
            notifyItemChanged(position)

            currentSelected = position
        }
    }
}