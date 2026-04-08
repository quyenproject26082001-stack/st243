package com.tempttt.ui.choose_character

import androidx.core.content.ContextCompat
import com.tempttt.R
import com.tempttt.core.base.BaseAdapter
import com.tempttt.core.extensions.gone
import com.tempttt.core.extensions.loadImage
import com.tempttt.core.extensions.tap
import com.tempttt.data.model.custom.CustomizeModel
import com.tempttt.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}


    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            binding.imvImage.maskDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.mask_hexagon)

            loadImage(item.avatar, imvImage, onDismissLoading = {
                sflShimmer.stopShimmer()
                sflShimmer.gone()
            })
            root.tap { onItemClick.invoke(position) }
        }
    }
}