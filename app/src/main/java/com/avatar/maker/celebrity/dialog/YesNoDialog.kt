package com.avatar.maker.celebrity.dialog

import android.app.Activity
import android.graphics.Color
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.hideNavigation
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseDialog
import com.avatar.maker.celebrity.core.extensions.strings
import com.avatar.maker.celebrity.databinding.DialogConfirmBinding

class YesNoDialog(
    val context: Activity,
    val title: Int,
    val description: Int,
    val isError: Boolean = false
) : BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
        initText()
        initBackground()
        if (isError) {
            binding.btnNo.gone()
        }
        context.hideNavigation()
        binding.tvTitle.isSelected = true
    }

    private fun initBackground() {
        binding.containerDialog.setBackgroundResource(R.drawable.bg_dialog_delete_exit)
        binding.btnNo.setBackgroundResource(R.drawable.bg_btn_permission_no)
        binding.btnYes.setBackgroundResource(R.drawable.bg_btn_permission_yes)
        val paddingVertical = (9 * context.resources.displayMetrics.density).toInt()
        binding.btnNo.setPadding(0, paddingVertical, 0, paddingVertical)
        binding.btnYes.setPadding(0, paddingVertical, 0, paddingVertical)
    }

    override fun initAction() {
        binding.apply {
            btnNo.tap { onNoClick.invoke() }
            btnYes.tap { onYesClick.invoke() }
            flOutSide.tap { onDismissClick.invoke() }
        }
    }

    override fun onDismissListener() {}

    private fun initText() {
        binding.apply {
            tvTitle.text = context.strings(title)
            tvDescription.text = context.strings(description)
            if (isError) {
                btnYes.text = context.strings(R.string.ok)
            }
        }
    }
}
