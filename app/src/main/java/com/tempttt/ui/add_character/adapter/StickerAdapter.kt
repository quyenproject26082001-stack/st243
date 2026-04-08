package com.tempttt.ui.add_character.adapter

import com.tempttt.core.base.BaseAdapter
import com.tempttt.core.extensions.loadImage
import com.tempttt.core.extensions.loadImageSticker
import com.tempttt.core.extensions.tap
import com.tempttt.data.model.SelectedModel
import com.tempttt.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImageSticker(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}