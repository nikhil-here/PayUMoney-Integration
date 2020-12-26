package com.application.payumoneyintegration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.payumoney.core.PayUmoneyConfig;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String hashFromServer;
    private String amount, fname, email, contact;
    private EditText etAmount, etFname, etEmail, etContact;
    private Button btnStartTransaction;

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_main_btn_start_transaction:
                getValues();
                launchPaymentFlow();
                break;
        }
    }


    private void launchPaymentFlow() {
        PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();
        payUmoneyConfig.setPayUmoneyActivityTitle("PayU Money Integration Demo");
        payUmoneyConfig.setDoneButtonText(" Pay ");

        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
        builder.setAmount(amount) //your amount
                .setTxnId(System.currentTimeMillis() + "") //unique transaction ID
                .setPhone(contact)
                .setProductName("Testing PayU Money") //you can set anything you want
                .setFirstName(fname)
                .setEmail(email)
                .setsUrl(Constants.SURL)
                .setfUrl(Constants.FURL)
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setUdf6("")
                .setUdf7("")
                .setUdf8("")
                .setUdf9("")
                .setUdf10("")
                .setIsDebug(Constants.DEBUG)
                .setKey(Constants.MERCHANT_KEY)
                .setMerchantId(Constants.MERCHANT_ID);
        try {
            PayUmoneySdkInitializer.PaymentParam mPaymentParams = builder.build();
            getHashFromServer(mPaymentParams);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getHashFromServer(final PayUmoneySdkInitializer.PaymentParam mPaymentParams) {
        Toast.makeText(MainActivity.this,"Getting Hash From Server ",Toast.LENGTH_SHORT).show();
        String url = Constants.GET_HASH;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hashFromServer = response;
                        Log.i(TAG, "onResponse: response "+response);
                        Toast.makeText(MainActivity.this,"Hash From server "+hashFromServer,Toast.LENGTH_SHORT).show();
                        if (hashFromServer.isEmpty() || hashFromServer.equals("")) {
                            Toast.makeText(MainActivity.this, "Could not generate hash", Toast.LENGTH_SHORT).show();
                        } else {
                            mPaymentParams.setMerchantHash(hashFromServer);
                            PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, MainActivity.this, R.style.PayUMoney, true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: "+error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Log.i(TAG, "getParams: "+mPaymentParams.getParams());
                return mPaymentParams.getParams();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result Code is -1 send from Payumoney activity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE);
            ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);

            // Check which object is non-null
            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {
                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    //Success Transaction
                } else {
                    //Failure Transaction
                }
                // Response from Payumoney
                String payuResponse = transactionResponse.getPayuResponse();
                // Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + payuResponse + "\n\n\n Merchant's Data: " + merchantResponse)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();
            } else if (resultModel != null && resultModel.getError() != null) {
                Log.d(TAG, "Error response : " + resultModel.getError().getTransactionResponse());
            } else {
                Log.d(TAG, "Both objects are null!");
            }
        }
    }



    private void getValues() {
        amount = etAmount.getText().toString();
        fname = etFname.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        contact = etContact.getText().toString().trim();
    }

    private void initListeners() {
        btnStartTransaction.setOnClickListener(this);
    }

    private void initViews() {
        etAmount = findViewById(R.id.activity_main_et_amount);
        etFname = findViewById(R.id.activity_main_et_fname);
        etEmail = findViewById(R.id.activity_main_et_email);
        etContact = findViewById(R.id.activity_main_et_contact);
        btnStartTransaction = findViewById(R.id.activity_main_btn_start_transaction);
    }


}