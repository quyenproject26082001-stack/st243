package com.avatar.maker.celebrity.ui.intro

import android.content.Context
import com.avatar.maker.celebrity.core.base.BaseAdapter
import com.avatar.maker.celebrity.core.extensions.loadImage
import com.avatar.maker.celebrity.core.extensions.select
import com.avatar.maker.celebrity.core.extensions.strings
import com.avatar.maker.celebrity.data.model.IntroModel
import com.avatar.maker.celebrity.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}