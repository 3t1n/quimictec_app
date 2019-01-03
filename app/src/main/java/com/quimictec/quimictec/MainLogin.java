package com.quimictec.quimictec;

        import android.Manifest;
        import android.content.Context;
        import android.content.ContextWrapper;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.location.LocationManager;
        import android.os.Build;
        import android.preference.PreferenceManager;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;
        import com.google.gson.JsonObject;
        import com.koushikdutta.async.future.FutureCallback;
        import com.koushikdutta.ion.Ion;


public class MainLogin extends AppCompatActivity {
    private EditText editEmailLogar, editSenhaLogar;
    private Button BtnLogar;
    private String  HOST = "https://quimictec.herokuapp.com/api/user/login";
    public static final String NOME_PREFERENCE = "INFORMACOES_LOGIN_AUTOMATICO";
    private String email;
    private String senha;
    private String token;
    private String token_acesso;
    private String id;
    public static final String KEY = "nome_chave";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainLogin.this);

        editEmailLogar = (EditText) findViewById(R.id.editEmailLogar);
        editSenhaLogar = (EditText) findViewById(R.id.editSenhaLogar);
        BtnLogar = (Button) findViewById(R.id.BtnLogar);

        BtnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }
    public  Context getContext(){
        Context mContext = MainLogin.this;
        return mContext;
    }
    private void login(){
        email = editEmailLogar.getText().toString();
        senha = editSenhaLogar.getText().toString();
        String URL = HOST;
        //valida se os campos estão vazios
        if(email.isEmpty()){
            Toast.makeText(MainLogin.this, "Insira um email", Toast.LENGTH_LONG).show();
        }else if(senha.isEmpty()){
            Toast.makeText(MainLogin.this, "Insira uma senha", Toast.LENGTH_LONG).show();
        }else {
            Ion.with(MainLogin.this)
                    .load(URL)
                    .setBodyParameter("email", email)
                    .setBodyParameter("password", senha)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            try {
                                String status = result.get("status").getAsString();
                                if(status.isEmpty() || status.equals("error")){
                                    Toast.makeText(MainLogin.this, "Credencias inválidas" , Toast.LENGTH_LONG).show();
                                }
                                else{
                                     token = result.get("token").getAsString();
                                     id = result.getAsJsonObject("user").get("id").getAsString();
                                    String nome = result.getAsJsonObject("user").get("name").getAsString();
                                    Toast.makeText(MainLogin.this, "Bem vindo  " + nome, Toast.LENGTH_LONG).show();
                                    Ponto.setDefaults("ja_abriu_app", "true", getContext());
                                    Ponto.setDefaults("email", email, getContext());
                                    Ponto.setDefaults("senha", senha, getContext());
                                    Ponto.setDefaults("token", token, getContext());
                                    Ponto.setDefaults("id", id, getContext());
                                    TelaDigital();
                                }
                            } catch (Exception erro) {
                                Toast.makeText(MainLogin.this, "Ops! Ocorreu um erro, " + erro, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }
    public void TelaDigital(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainLogin.this);
        Ponto.getDefaults("token",getContext());
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainLogin.this, "Permitir GPS" , Toast.LENGTH_LONG).show();
            }else{
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                String lat = String.valueOf(latitude);
                String longe = String.valueOf(longitude);
                Ponto.setDefaults("latitude", lat, getContext());
                Ponto.setDefaults("longitude", longe, getContext());
                //Ponto.getDefaults("token_acesso", token_acesso);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }



    }

}
