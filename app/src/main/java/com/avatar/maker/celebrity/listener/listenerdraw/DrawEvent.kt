package com.avatar.maker.celebrity.listener.listenerdraw

import android.view.MotionEvent
import com.avatar.maker.celebrity.core.custom.drawview.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}