package com.tempttt.ui.intro

import android.content.Context
import com.tempttt.core.base.BaseAdapter
import com.tempttt.core.extensions.loadImage
import com.tempttt.core.extensions.select
import com.tempttt.core.extensions.strings
import com.tempttt.data.model.IntroModel
import com.tempttt.databinding.ItemIntroBinding

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