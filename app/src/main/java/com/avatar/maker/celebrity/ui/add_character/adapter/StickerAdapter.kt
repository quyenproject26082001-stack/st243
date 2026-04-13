package com.avatar.maker.celebrity.ui.add_character.adapter

import com.avatar.maker.celebrity.core.base.BaseAdapter
import com.avatar.maker.celebrity.core.extensions.loadImage
import com.avatar.maker.celebrity.core.extensions.loadImageSticker
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.data.model.SelectedModel
import com.avatar.maker.celebrity.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImageSticker(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}