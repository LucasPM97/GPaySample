package com.lucas.gpay.ui.completePay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.samples.wallet.PaymentsUtil
import com.google.android.gms.wallet.*
import com.lucas.gpay.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_complete_pay.*
import kotlinx.android.synthetic.main.main_fragment.avatar_image
import org.json.JSONObject

class CompletePayFragment : Fragment() {

    companion object {
        const val LOAD_PAYMENT_DATA_REQUEST_CODE: Int = 1004
    }

    private lateinit var viewModel: CompletePayViewModel
    private lateinit var paymentsClient: PaymentsClient
    private var mountToPay: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_complete_pay, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        context?.let {
            paymentsClient = PaymentsUtil.createPaymentsClient(it)
        }

        viewModel = ViewModelProvider(this).get(CompletePayViewModel::class.java)

        setObservers()

        getNavArgs()

        Picasso.get().load(viewModel.getAvatarUrl()).transform(CropCircleTransformation())
            .into(avatar_image);

        possiblyShowGooglePayButton()

        googlePayButton.setOnClickListener { requestPayment() }


    }

    private fun setObservers(){
        val price: MutableLiveData<Int?>? = viewModel.getMountToPay()

        price?.observe(viewLifecycleOwner, Observer {
            it?.let {
                finalMount_text.text = "Donate $${it.toString()} to me"
                mountToPay = it
            }
        })
    }

    private fun getNavArgs(){
        arguments?.let {bundle ->
            val args = CompletePayFragmentArgs.fromBundle(bundle)

            viewModel.setMountToPay(args.mountToPay)
        }
    }

    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            googlePayButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                context,
                "Unfortunately, Google Pay is not available on this device",
                Toast.LENGTH_LONG).show();
        }
    }

    private fun requestPayment() {

        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(mountToPay.toString())
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            activity?.let {
                AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request), it, LOAD_PAYMENT_DATA_REQUEST_CODE)
            }
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result
     * from an Activity](https://developer.android.com/training/basics/intents/result)
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // value passed in AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }
                    Activity.RESULT_CANCELED -> {
                        // Nothing to do here normally - the user simply cancelled without selecting a
                        // payment method.
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }
                // Re-enables the Google Pay payment button.
                googlePayButton.isClickable = true
            }
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return
        val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")

        Toast.makeText(context,"Success :D", Toast.LENGTH_LONG)

    }

    private fun handleError(statusCode: Int) {
        Toast.makeText(context,"Failed D:", Toast.LENGTH_LONG)
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

}
