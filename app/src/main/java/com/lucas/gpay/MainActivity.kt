package com.lucas.gpay

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.lucas.gpay.ui.completePay.CompletePayFragment
import kotlinx.android.synthetic.main.fragment_complete_pay.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var navController:NavController;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // value passed in AutoResolveHelper
            CompletePayFragment.LOAD_PAYMENT_DATA_REQUEST_CODE -> {
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
        try {
            val paymentInformation = paymentData.toJson() ?: return
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")

            Toast.makeText(this,"Success :D", Toast.LENGTH_LONG).show()
        }
        catch (ex:Exception){
            print(ex)
        }
    }

    private fun handleError(statusCode: Int) {
        Toast.makeText(this,"Failed D:", Toast.LENGTH_LONG).show()
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

}
