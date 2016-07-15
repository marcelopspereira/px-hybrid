package com.example.plugin;


import android.app.Activity;
import android.content.Intent;
import android.util.StringBuilderPrinter;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mercadopago.callbacks.Callback;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Sites;
import com.mercadopago.model.Token;


import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;


public class MercadoPagoPlugin extends CordovaPlugin {
    private CallbackContext callback = null;
    
    
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        
        
        if (action.equals("startActivity")) {
            cordova.setActivityResultCallback (this);
            new MercadoPago.StartActivityBuilder()
            .setActivity(this.cordova.getActivity())
            .setPublicKey("TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a")
            .setCheckoutPreferenceId("150216849-ceed1ee4-8ab9-4449-869f-f4a8565d386f")
            .startCheckoutActivity();
            
            callback = callbackContext;
            
            
            return true;
            
            
        } else if (action.equals("startPaymentVault")){
            cordova.setActivityResultCallback (this);
            callback = callbackContext;
            BigDecimal ammount = new BigDecimal(data.getInt(1));
            new MercadoPago.StartActivityBuilder()
            .setActivity(this.cordova.getActivity())
            .setPublicKey(data.getString(0))
            .setAmount(ammount)
            .setSite(Sites.ARGENTINA)
            .startPaymentVaultActivity();

            return true;

        } else if (action.equals("startCardWithoutInstallments")){
            cordova.setActivityResultCallback (this);
            callback = callbackContext;
            new MercadoPago.StartActivityBuilder()
                    .setActivity(this.cordova.getActivity())
                    .setPublicKey(data.getString(0))
                    .startGuessingCardActivity();

            return true;

        } else if (action.equals("startCardWithInstallments")){
            cordova.setActivityResultCallback (this);
            callback = callbackContext;
            BigDecimal ammount = new BigDecimal(data.getInt(1));

            new MercadoPago.StartActivityBuilder()
                    .setActivity(this.cordova.getActivity())
                    .setPublicKey(data.getString(0))
                    .setAmount(ammount)
                    .setSite(Sites.ARGENTINA)
                    .startCardVaultActivity();

            return true;

        } else if (action.equals("startPaymentMethodsComponent")){
            cordova.setActivityResultCallback (this);
            callback = callbackContext;

            new MercadoPago.StartActivityBuilder()
                    .setActivity(this.cordova.getActivity())
                    .setPublicKey(data.getString(0))
                    .startPaymentMethodsActivity();

            return true;

        } else if (action.equals("startPaymentMethodsComponent")) {
            cordova.setActivityResultCallback(this);
            callback = callbackContext;
            PaymentMethod paymentMethod = new PaymentMethod();

            new MercadoPago.StartActivityBuilder()
                    .setActivity(this.cordova.getActivity())
                    .setPublicKey(data.getString(0))
                    .setPaymentMethod(paymentMethod)
                    .startIssuersActivity();

            return true;

        } else if (action.equals("getPaymentMethods")){
            MercadoPago mercadoPago = new MercadoPago.Builder()
                    .setContext(this.cordova.getActivity())
                    .setPublicKey(data.getString(0))
                    .build();

            mercadoPago.getPaymentMethods(new Callback<List<PaymentMethod>>() {
                @Override
                public void success(List<PaymentMethod> paymentMethods) {
                    callback.success(paymentMethods.toArray().toString());
                }

                @Override
                public void failure(ApiException error) {
                   callback.success("error");
                }
            });

            return true;
        } else {
            
            
            return false;
            
            
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MercadoPago.PAYMENT_VAULT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentMethod mppaymentMethod = (PaymentMethod) data.getSerializableExtra("paymentMethod");
                Issuer mpissuer = (Issuer) data.getSerializableExtra("issuer");
                Token mptoken = (Token) data.getSerializableExtra("token");
                PayerCost mppayerCost = (PayerCost) data.getSerializableExtra("payerCost");
                Gson gson = new Gson();
                String paymentMethod = gson.toJson(mppaymentMethod);
                String issuer = gson.toJson(mpissuer);
                String token = gson.toJson(mptoken);
                String payerCost = gson.toJson(mppayerCost);
                JSONObject js = new JSONObject();
                try {
                    js.put("payment_method", paymentMethod);
                    js.put("issuer", issuer);
                    js.put("token", token);
                    js.put("payer_cost", payerCost);
                    
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                callback.success(js.toString());
            }
        }
        if (requestCode == MercadoPago.CHECKOUT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Payment payment=(Payment)data.getSerializableExtra("payment");
                Gson gson = new Gson();
                String json = gson.toJson(payment);
                callback.success(json);
            }
        }
        
        
    }
}
