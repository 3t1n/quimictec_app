package com.quimictec.quimictec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;


    public FingerprintHandler(Context context) {
        this.context = context;

    }

    public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {

        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);


    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("Erro ao autenticar " + errString, false);
    }

    @Override
    public void onAuthenticationFailed() {
        this.update("Falha ao autenticar, tente novamente ", false);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Error " + helpString, false);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("Atenticado com sucesso ", true);
    }

    private void update(String s, boolean b) {

        final TextView paralabel = (TextView) ((Activity) context).findViewById(R.id.paraLabel);
        final ImageView imageView = (ImageView) ((Activity) context).findViewById(R.id.fingerPrintImage);

        paralabel.setText(s);

        if (b == false) {
            //se falhar
            paralabel.setTextColor(ContextCompat.getColor(context, R.color.error));
            imageView.setImageResource(R.mipmap.action_error);
        } else {
            //se sucess
            paralabel.setTextColor(ContextCompat.getColor(context, R.color.sucess));
            imageView.setImageResource(R.mipmap.action_done);
            String latitude = Ponto.getDefaults("latitude", context);
            String longitude = Ponto.getDefaults("longitude", context);
            String token = "Bearer " + Ponto.getDefaults("token", context);
            String URL = "http://quimictec.herokuapp.com/api/ponto";
            //paralabel.setText(token);
            Ion.with(context)
                    .load(URL)
                    .setHeader("Authorization", token)
                    .setBodyParameter("latitude", latitude)
                    .setBodyParameter("longitude", longitude)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            try {
                                //String nome = result.getAsJsonObject("user").get("name").getAsString();
                                String status = result.get("status").getAsString();
                                if(status.isEmpty() || status.equals("error_fora_trabalho")){
                                    paralabel.setTextColor(ContextCompat.getColor(context, R.color.error));
                                    imageView.setImageResource(R.mipmap.action_error);
                                    paralabel.setText("Você está fora da área de trabalho");
                                }else if(status.equals("error_inserir")){
                                    paralabel.setTextColor(ContextCompat.getColor(context, R.color.error));
                                    imageView.setImageResource(R.mipmap.action_error);
                                    paralabel.setText("Gps com erros");
                                }else if(status.equals("erro_ja_efetuou")) {
                                    paralabel.setTextColor(ContextCompat.getColor(context, R.color.error));
                                    imageView.setImageResource(R.mipmap.action_error);
                                    paralabel.setText("Você já efetuou sua entrada e saída hoje");
                                }
                                else if(status.equals("success")){
                                    paralabel.setTextColor(ContextCompat.getColor(context, R.color.sucess));
                                    imageView.setImageResource(R.mipmap.action_done);
                                    paralabel.setText("Ponto efetuado com sucesso!");

                                }
                            }
                            catch (Exception erro){
                                Toast.makeText(context, "Ops! Ocorreu um erro, " + erro, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}
