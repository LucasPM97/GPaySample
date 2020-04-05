package com.lucas.gpay.ui.completePay

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CompletePayViewModel : ViewModel() {

    private var avatarUrl: String? = "https://github.com/LucasPM97.png"

    fun getAvatarUrl():String?{
        return avatarUrl
    }

    private var mountToPay:MutableLiveData<Int?>? = null

    fun getMountToPay():MutableLiveData<Int?>? {
        if (mountToPay == null){
            mountToPay = MutableLiveData<Int?>()
            setMountToPay(0)
        }
        return mountToPay
    }

    fun setMountToPay(price:Int){
        mountToPay?.value = price
    }
}
