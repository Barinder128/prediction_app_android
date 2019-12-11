package com.example.priceforecast;
//---importing libraries-------
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    // declare global variables
    EditText mEditText;
    EditText cEditText;
    EditText xEditText;
    TextView outputTextView;
    Button submitButton;
    String buttonState = "calculate";
    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //To check screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        if(height<1500){
            setContentView(R.layout.activity_main1);            //shows layout for small screen phones
        }
        else {
            setContentView(R.layout.activity_main);             //show layout for large screen phones
        }
        // Provided reference to view of elements
        mEditText = findViewById(R.id.mEditText);
        cEditText = findViewById((R.id.cEditText));
        xEditText = findViewById(R.id.xEditText);
        outputTextView = findViewById(R.id.outputTextView);
        submitButton = findViewById(R.id.submitButton);
    }

    //Method to hide keyboard when we touch outside editText fields in Android App
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


    //to enable app exit on double back press only
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        //Handler().postDelayed will call run after 2 seconds to set doubleBackToExitPressedOnce = false
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    //Function To check if all editText fields have valid values before posting data to server
    public boolean inputFieldValidation(String c_value, String m_value, String x_value){
        if (c_value.equals("")) {
            cEditText.requestFocus();
            Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
        } else if (m_value.equals("")) {
            mEditText.requestFocus();
            Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
        } else if (x_value.equals("")) {
            xEditText.requestFocus();
            Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
        } else if (c_value.substring(0,1).equals(".")) {
            cEditText.requestFocus();
            Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (m_value.substring(0,1).equals(".")) {
            mEditText.requestFocus();
            Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (x_value.substring(0,1).equals(".")) {
            xEditText.requestFocus();
            Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (c_value.substring(c_value.length()-1).equals(".")) {
            cEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (m_value.substring(m_value.length()-1).equals(".")) {
            mEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (x_value.substring(x_value.length()-1).equals(".")) {
            xEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (c_value.substring(c_value.length()-1).equals("-")) {
            cEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with negative sign (-)", Toast.LENGTH_SHORT).show();
        } else if (m_value.substring(m_value.length()-1).equals("-")) {
            mEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with negative sign (-)", Toast.LENGTH_SHORT).show();
        } else if (x_value.substring(x_value.length()-1).equals("-")) {
            xEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with negative sign (-)", Toast.LENGTH_SHORT).show();
        } else{
            return true;
        }
        return false;
    }

    //On clicking CALCULATE button this method is called
    public void postData(View view){
        //To check the state of button and change it if condition is true
        if(buttonState.equals("new_value")){
            submitButton.setText("Calculate");
            buttonState="calculate";
            xEditText.setText("");
            outputTextView.setText("");
            xEditText.setEnabled(true);
            cEditText.requestFocus();
        } else {
            String m_value = mEditText.getText().toString();
            String c_value = cEditText.getText().toString();
            String x_value = xEditText.getText().toString();
            //Method Call to check if all editText fields have valid values before posting data to server
            boolean inputFieldCheck = inputFieldValidation(c_value, m_value, x_value);
            if(inputFieldCheck){
                outputTextView.setText("Calculating Value");
                submitButton.setClickable(false);
                buttonState="fetching_price";
                cEditText.setEnabled(false);
                mEditText.setEnabled(false);
                xEditText.setEnabled(false);

                //Retrofit Class defining base url where app needs to post data
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://prediction1-rest-api.herokuapp.com/")
                        .build();
                //Retrofit class generates an implementation of Api interface
                Api api = retrofit.create(Api.class);
                String json = "{\n" +
                        "\t\"m_value\": " + Float.parseFloat(m_value) + ",\n" +
                        "\t\"c_value\": " + Float.parseFloat(c_value) + ",\n" +
                        "\t\"x_value\": " + Float.parseFloat(x_value) + "\n" +
                        "}";

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
                //This method POST request and we receive return data in response
                api.postUser(requestBody).enqueue(new Callback<ResponseBody>() {
                    //Method executed if we receive response from server
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            String json = response.body().string();     //receive response from server
                            JSONObject jsonOb = new JSONObject(json);
                            double y_value = Double.parseDouble(jsonOb.optString("y"));
                            if(y_value>10000000) {
                                String _y_value = Double.toString(y_value);
                                int y_value_index = _y_value.indexOf('E');
                                double __y_value = Double.parseDouble(_y_value.substring(0,y_value_index));
                                outputTextView.setText("y= " + String.format("%.2f", __y_value) + "E" + _y_value.substring(y_value_index+1));          //Price displayed in scientific notation
                            }
                            else
                                outputTextView.setText("y= " + String.format("%.2f", y_value));     //Price displayed as double rounded off to two decimal places
                            buttonState="new_value";
                            submitButton.setClickable(true);
                            submitButton.setText("New Value");
                        } catch (Exception e) {
                            e.printStackTrace();
                            buttonState="calculate";
                            submitButton.setClickable(true);
                            outputTextView.setText("");
                            xEditText.setEnabled(true);
                        }
                    }
                    //Method executed if request fails
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(t.toString().equals("java.net.UnknownHostException:" +
                                " Unable to resolve host \"prediction1-rest-api.herokuapp.com\": No address associated with hostname"))
                            Toast.makeText(getApplicationContext(), "Please Check Your Network Connection", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "Server Not Responding. Please Try Again, Later.", Toast.LENGTH_SHORT).show();
                        buttonState="calculate";
                        submitButton.setClickable(true);
                        outputTextView.setText("");
                        xEditText.setEnabled(true);
                    }
                });

            }
        }
    }

    //On clicking RESET ALL button this method is called
    public void restAll(View view){
        submitButton.setText("Calculate");
        buttonState="calculate";
        cEditText.setText("");
        mEditText.setText("");
        xEditText.setText("");
        outputTextView.setText("");
        cEditText.setEnabled(true);
        mEditText.setEnabled(true);
        xEditText.setEnabled(true);
        cEditText.requestFocus();
    }
}
