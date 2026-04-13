package com.avatar.maker.celebrity.ui.add_character.adapter

import android.graphics.drawable.GradientDrawable
import android.util.Log
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseAdapter
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.data.model.SelectedModel
import com.avatar.maker.celebrity.databinding.ItemBackgroundColorBinding

class BackgroundColorAdapter :
    BaseAdapter<SelectedModel, ItemBackgroundColorBinding>(ItemBackgroundColorBinding::inflate) {
    var onChooseColorClick: (() -> Unit) = {}
    var onBackgroundColorClick: ((Int, Int) -> Unit) = {_,_ ->}

    var currentSelected = -1
    override fun onBind(binding: ItemBackgroundColorBinding, item: SelectedModel, position: Int) {
        Log.d("BackgroundColorAdapter", "onBind position=$position, color=${String.format("#%06X", 0xFFFFFF and item.color)}, isSelected=${item.isSelected}, path=${item.path}")

        binding.apply {
            vFocus.isVisible = true
            if (item.isSelected) {
                vFocus.setBackgroundResource(R.drawable.bg_stroke_gradient_circle)
            } else {
                vFocus.setBackgroundResource(R.drawable.bg_stroke_gradient_circle_uslt)
            }
            // Set circular stroke for position 0, regular stroke for others


            if (position == 0) {
                Log.d("BackgroundColorAdapter", "Position 0: Loading img with CircleCrop")
                Glide.with(root.context).clear(imvColor)
                imvColor.background = null
                Glide.with(root.context)
                    .load(R.drawable.img)
                    .transform(CircleCrop())
                    .into(imvColor)
                root.tap { onChooseColorClick.invoke() }
            } else {
                Log.d("BackgroundColorAdapter", "Position $position: Setting color background")
                Glide.with(root.context).clear(imvColor)
                imvColor.setImageDrawable(null)
                imvColor.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(item.color)
                }
                root.tap { onBackgroundColorClick.invoke(item.color, position) }
            }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>){
        Log.d("BackgroundColorAdapter", "submitItem called with position=$position")
        Log.d("BackgroundColorAdapter", "Item at position 0: color=${String.format("#%06X", 0xFFFFFF and list[0].color)}, isSelected=${list[0].isSelected}")
        if (position == 0) {
            Log.d("BackgroundColorAdapter", "WARNING: Position 0 was selected!")
        }

        items.clear()
        items.addAll(list)

        if (position != currentSelected){
            Log.d("BackgroundColorAdapter", "Notifying changes for positions $currentSelected and $position")
            notifyItemChanged(currentSelected)
            notifyItemChanged(position)
            currentSelected = position
        } else {
            Log.d("BackgroundColorAdapter", "Notifying change for position $position")
            notifyItemChanged(position)
        }
    }
}