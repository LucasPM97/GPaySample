package com.lucas.gpay.ui.completePay

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lucas.gpay.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_complete_pay.*
import kotlinx.android.synthetic.main.main_fragment.avatar_image

class CompletePayFragment : Fragment() {

    private lateinit var viewModel: CompletePayViewModel
    private var mountToPay: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_complete_pay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CompletePayViewModel::class.java)

        setObversers()

        getNavArgs()

        Picasso.get().load(viewModel.getAvatarUrl()).transform(CropCircleTransformation())
            .into(avatar_image);

    }

    fun setObversers(){
        val price: MutableLiveData<Int?>? = viewModel.getMountToPay()

        price?.observe(viewLifecycleOwner, Observer {
            it?.let {
                finalMount_text.text = "Donate $${it.toString()} to me"
                mountToPay = it
            }
        })
    }

    fun getNavArgs(){
        arguments?.let {bundle ->
            val args = CompletePayFragmentArgs.fromBundle(bundle)

            viewModel.setMountToPay(args.mountToPay)
        }
    }
}
