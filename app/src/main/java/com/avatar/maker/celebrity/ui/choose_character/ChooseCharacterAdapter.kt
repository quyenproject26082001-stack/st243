package com.avatar.maker.celebrity.ui.choose_character

import androidx.core.content.ContextCompat
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseAdapter
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.loadImage
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.data.model.custom.CustomizeModel
import com.avatar.maker.celebrity.databinding.ItemChooseAvatarBinding

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