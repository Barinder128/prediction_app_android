package com.example.priceforecast;
//---importing libraries-------
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
    EditText widthEditText;
    EditText lengthEditText;
    EditText heightEditText;
    TextView outputTextView;
    TextView calculatingTextView;
    Button submitButton;
    String buttonState = "calculate";
    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Provided reference to view of elements
        widthEditText = findViewById(R.id.widthEditText);
        lengthEditText = findViewById((R.id.lengthEditText));
        heightEditText = findViewById(R.id.heightEditText);
        outputTextView = findViewById(R.id.outputTextView);
        submitButton = findViewById(R.id.submitButton);
        calculatingTextView = findViewById(R.id.calculatingTextView);
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

    //On clicking Calculate button this method is called
    public void postData(View view){
        //To check the state of button and change it if condition is true
        if(buttonState.equals("reset")){
            submitButton.setText("Calculate");
            buttonState="calculate";
            lengthEditText.setText("");
            widthEditText.setText("");
            heightEditText.setText("");
            outputTextView.setText("");
            lengthEditText.setEnabled(true);
            widthEditText.setEnabled(true);
            heightEditText.setEnabled(true);
            lengthEditText.requestFocus();
        } else {

            String width = widthEditText.getText().toString();
            String length = lengthEditText.getText().toString();
            String height = heightEditText.getText().toString();

            //To check if all editText fields have valid values before posting data to server
            if (length.equals("")) {
                lengthEditText.requestFocus();
                Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
            } else if (width.equals("")) {
                widthEditText.requestFocus();
                Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
            } else if (height.equals("")) {
                heightEditText.requestFocus();
                Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
            } else if (length.substring(0,1).equals(".")) {
                lengthEditText.requestFocus();
                Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
            } else if (width.substring(0,1).equals(".")) {
                widthEditText.requestFocus();
                Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
            } else if (height.substring(0,1).equals(".")) {
                heightEditText.requestFocus();
                Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
            } else if (length.substring(length.length()-1).equals(".")) {
                lengthEditText.requestFocus();
                Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
            } else if (width.substring(width.length()-1).equals(".")) {
                widthEditText.requestFocus();
                Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
            } else if (height.substring(height.length()-1).equals(".")) {
                heightEditText.requestFocus();
                Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
            } else {
                calculatingTextView.setText("Calculating Price");
                submitButton.setClickable(false);
                buttonState="fetching_price";
                lengthEditText.setEnabled(false);
                widthEditText.setEnabled(false);
                heightEditText.setEnabled(false);

                //Retrofit Class defining base url where app needs to post data
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://prediction1-rest-api.herokuapp.com/")
                        .build();

                //Retrofit class generates an implementation of Api interface
                Api api = retrofit.create(Api.class);
                //json data which App has to POST to server
                String json = "{\n" +
                        "\t\"width\": " + width + ",\n" +
                        "\t\"length\": " + length + ",\n" +
                        "\t\"height\": " + height + "\n" +
                        "}";

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
                //This method POST request and we receive return data in response
                api.postUser(requestBody).enqueue(new Callback<ResponseBody>() {
                    //Method executed if we receive response from server
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            calculatingTextView.setText("");
                            String json = response.body().string();     //receive reponse from server
                            JSONObject jsonOb = new JSONObject(json);
                            float price = Float.parseFloat(jsonOb.optString("price"));
                            if(price>10000000)
                                outputTextView.setText("$" + (price));            //Price displayed in scientific notation
                            else
                                outputTextView.setText("$" + String.format("%.2f", price));     //Price displayed as float rounded off to two decimal places
                            buttonState="reset";
                            submitButton.setClickable(true);
                            submitButton.setText("Reset");
                        } catch (Exception e) {
                            e.printStackTrace();
                            buttonState="calculator";
                            submitButton.setClickable(true);
                            outputTextView.setText("");
                            calculatingTextView.setText("");
                            lengthEditText.setEnabled(true);
                            widthEditText.setEnabled(true);
                            heightEditText.setEnabled(true);
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
                        buttonState="calculator";
                        submitButton.setClickable(true);
                        outputTextView.setText("");
                        calculatingTextView.setText("");
                        lengthEditText.setEnabled(true);
                        widthEditText.setEnabled(true);
                        heightEditText.setEnabled(true);
                    }
                });

            }
        }
    }
}
