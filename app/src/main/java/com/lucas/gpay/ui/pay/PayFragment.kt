package com.lucas.gpay.ui.pay

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lucas.gpay.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_pay.*
import kotlinx.android.synthetic.main.main_fragment.avatar_image

class PayFragment : Fragment() {

    private lateinit var viewModel: PayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pay, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PayViewModel::class.java)

        Picasso.get().load(viewModel.getAvatarUrl()).transform(CropCircleTransformation()).into(avatar_image);

        val price:MutableLiveData<Int?>? = viewModel.getMountToPay()

        price?.observe(viewLifecycleOwner, Observer {
            priceMount_text.text = "$ ${it.toString()}"
        })

        optionList.setOnCheckedChangeListener{ group, checkedId ->
                val newPrice =  when(checkedId){
                    price1.id-> 1
                    price2.id-> 2
                    price3.id-> 5
                    price4.id-> 10
                    else -> 0
                }
            viewModel.setMountToPay(newPrice)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )
    }
}
