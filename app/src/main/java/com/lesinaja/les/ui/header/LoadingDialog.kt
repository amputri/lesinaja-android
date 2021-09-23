package com.lesinaja.les.ui.header

import android.app.Activity
import android.app.AlertDialog
import com.lesinaja.les.R

class LoadingDialog(val mActivity: Activity) {
    private lateinit var isdialog: AlertDialog
    fun startLoading(){
        val infalter = mActivity.layoutInflater
        val dialogView = infalter.inflate(R.layout.dialog_loading,null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        isdialog = builder.create()
        isdialog.show()
        isdialog.setCancelable(false)
        isdialog.setCanceledOnTouchOutside(false)
    }

    fun isDismiss(){
        isdialog.dismiss()
    }
}