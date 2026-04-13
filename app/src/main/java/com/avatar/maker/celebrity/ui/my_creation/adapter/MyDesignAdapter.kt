package com.avatar.maker.celebrity.ui.my_creation.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import androidx.core.content.ContextCompat
import com.bumptech.glide.signature.ObjectKey
import com.avatar.maker.celebrity.R
import androidx.recyclerview.widget.RecyclerView
import com.avatar.maker.celebrity.core.base.BaseAdapter
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.core.extensions.visible
import com.avatar.maker.celebrity.data.model.MyAlbumModel
import com.avatar.maker.celebrity.databinding.ItemMyDesignBinding
import java.io.File

class MyDesignAdapter : BaseAdapter<MyAlbumModel, ItemMyDesignBinding>(ItemMyDesignBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}
    var onLongClick: ((Int) -> Unit) = {}
    var onItemTick: ((Int) -> Unit) = {}

    var onDeleteClick: ((String) -> Unit) = {}

    // DiffUtil optimization
    override fun areItemsTheSame(oldItem: MyAlbumModel, newItem: MyAlbumModel): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: MyAlbumModel, newItem: MyAlbumModel): Boolean {
        return oldItem == newItem
    }

    override fun onBind(binding: ItemMyDesignBinding, item: MyAlbumModel, position: Int) {
        binding.apply {
            imvImage.maskDrawable = ContextCompat.getDrawable(root.context, R.drawable.mask_hexagon)

            // Optimized Glide loading with thumbnail, size override, and caching
            val file = File(item.path)
            Glide.with(root.context)
                .load(file)
                .thumbnail(0.1f)
                .override(400, 400)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(ObjectKey(file.lastModified()))
                .into(imvImage)

            if (item.isShowSelection) {
                btnSelect.visible()
                btnDelete.gone()
            } else {
                btnSelect.gone()
                btnDelete.visible()
            }

            if (item.isSelected) {
                btnSelect.setImageResource(R.drawable.ic_selected)
            } else {
                btnSelect.setImageResource(R.drawable.ic_not_select)
            }

            root.tap { onItemClick.invoke(item.path) }

            root.setOnLongClickListener {
                if (items.any { album -> album.isShowSelection }) return@setOnLongClickListener false
                val (rv, rvChild) = findRecyclerView(root) ?: return@setOnLongClickListener false
                val actualPos = rv.getChildAdapterPosition(rvChild)
                if (actualPos == RecyclerView.NO_POSITION) return@setOnLongClickListener false
                onLongClick.invoke(actualPos)
                true
            }
            btnDelete.tap { onDeleteClick.invoke(item.path) }
            btnSelect.tap {
                val (rv, rvChild) = findRecyclerView(root) ?: return@tap
                val actualPos = rv.getChildAdapterPosition(rvChild)
                if (actualPos != RecyclerView.NO_POSITION) onItemTick.invoke(actualPos)
            }
        }
    }

    private fun findRecyclerView(view: android.view.View): Pair<RecyclerView, android.view.View>? {
        var child: android.view.View = view
        var parent = view.parent
        while (parent != null) {
            if (parent is RecyclerView) return Pair(parent, child)
            child = parent as? android.view.View ?: return null
            parent = child.parent
        }
        return null
    }
}