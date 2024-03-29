package com.example.login1.Activities;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.login1.Models.Usuario;
import com.example.login1.R;
import com.example.login1.Splash.SplashActivity;
import com.example.login1.Utils.Util;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

public class login extends AppCompatActivity {
    private TextView textViewEmail;
    private TextView textViewPassword;
    private Button btnLogin;
    private Switch switchRemember;
    private SharedPreferences prefs;
    public static String UserToken="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindUI();
        switchRemember=findViewById(R.id.switchRemember);
        prefs=getSharedPreferences("preferences", Context.MODE_PRIVATE);
        setCredentialsIfExist();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email= textViewEmail.getText().toString();
                String password = textViewPassword.getText().toString();
                if (login(email)){
                    getToken(email,password,com.example.login1.Activities.login.this);
                }



            }
        });

    }

    private void setCredentialsIfExist(){
        String email= Util.getUserMailPrefs(prefs);
        String pass= Util.getUserPassPrefs(prefs);
        boolean check= Util.getUserRemember(prefs);
        if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(pass)){
            textViewEmail.setText(email);
            textViewPassword.setText(pass);
            switchRemember.setChecked(check);
        }
    }

    private void saveOnPreferences(String email,String password){
        if (switchRemember.isChecked()){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("email",email);
            editor.putString("pass",password);
            editor.putString("token",UserToken);
            editor.putBoolean("remember",switchRemember.isChecked());
            editor.apply();
        }
    }

    private boolean login(String email){
        if (!isValidEmail(email)){
            Toast.makeText(this,"Email is not valid, plase try again",Toast.LENGTH_LONG).show();
            return false;
        }else
            return true;

    }
    private void bindUI(){
        textViewEmail = (TextView) findViewById(R.id.editTextEmail);
        textViewPassword = (TextView) findViewById(R.id.editTextPassword);
        btnLogin= (Button) findViewById(R.id.buttonLogin);
        switchRemember = (Switch) findViewById(R.id.switchRemember);
    }
    private boolean isValidEmail(String email){
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private void pantallaVendedor(){
        Intent intent = new Intent(this, ActivityMenuVendedor.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void pantallaSurtidor(){
        Intent intent = new Intent(this, ActivitySurtidor.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void removeSharedPreferences(){
        prefs.edit().clear().apply();

    }
    public void getToken(final String email, final String password,Context context){
        String url = "http://pvmovilbackend.eastus.azurecontainer.io/api/Usuarios/Login/";
        final JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("Email",email);
            jsonBody.put("Password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        removeSharedPreferences();
                        try {
                            UserToken=response.getJSONObject("Data").getString("Token");

                            if (switchRemember.isChecked()){
                                saveOnPreferences(email,password);
                            }

                            Gson gson = new Gson();
                            SplashActivity.userData = gson.fromJson(response.getJSONObject("Data").getJSONObject("Usuario").toString(), Usuario.class);

                            if (SplashActivity.userData.getRol().equals("Vendedor")){
                                pantallaVendedor();
                            }else
                            if (SplashActivity.userData.getRol().equals("Surtidor")){
                                pantallaSurtidor();
                            }else Toast.makeText(com.example.login1.Activities.login.this,"Usuario invalido",Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(com.example.login1.Activities.login.this,error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}
