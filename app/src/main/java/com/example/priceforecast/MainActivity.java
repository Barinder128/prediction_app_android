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
    EditText widthEditText;
    EditText lengthEditText;
    EditText depthEditText;
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
        widthEditText = findViewById(R.id.widthEditText);
        lengthEditText = findViewById((R.id.lengthEditText));
        depthEditText = findViewById(R.id.depthEditText);
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
    public boolean inputFieldValidation(String length, String width, String depth){
        if (length.equals("")) {
            lengthEditText.requestFocus();
            Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
        } else if (width.equals("")) {
            widthEditText.requestFocus();
            Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
        } else if (depth.equals("")) {
            depthEditText.requestFocus();
            Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
        } else if (length.substring(0,1).equals(".")) {
            lengthEditText.requestFocus();
            Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (width.substring(0,1).equals(".")) {
            widthEditText.requestFocus();
            Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (depth.substring(0,1).equals(".")) {
            depthEditText.requestFocus();
            Toast.makeText(this, "Input Number should not start with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (length.substring(length.length()-1).equals(".")) {
            lengthEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (width.substring(width.length()-1).equals(".")) {
            widthEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
        } else if (depth.substring(depth.length()-1).equals(".")) {
            depthEditText.requestFocus();
            Toast.makeText(this, "Input Number should not end with .(dot)", Toast.LENGTH_SHORT).show();
        } else{
            return true;
        }
        return false;
    }

    //On clicking Calculate button this method is called
    public void postData(View view){
        //To check the state of button and change it if condition is true
        if(buttonState.equals("reset")){
            submitButton.setText("Calculate");
            buttonState="calculate";
            lengthEditText.setText("");
            widthEditText.setText("");
            depthEditText.setText("");
            outputTextView.setText("");
            lengthEditText.setEnabled(true);
            widthEditText.setEnabled(true);
            depthEditText.setEnabled(true);
            lengthEditText.requestFocus();
        } else {
            String width = widthEditText.getText().toString();
            String length = lengthEditText.getText().toString();
            String depth = depthEditText.getText().toString();
            //Method Call to check if all editText fields have valid values before posting data to server
            boolean inputFieldCheck = inputFieldValidation(length, width, depth);
            if(inputFieldCheck){
                outputTextView.setText("Calculating Price");
                submitButton.setClickable(false);
                buttonState="fetching_price";
                lengthEditText.setEnabled(false);
                widthEditText.setEnabled(false);
                depthEditText.setEnabled(false);

                //Retrofit Class defining base url where app needs to post data
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://prediction1-rest-api.herokuapp.com/")
                        .build();
                //Retrofit class generates an implementation of Api interface
                Api api = retrofit.create(Api.class);
                String json = "{\n" +
                        "\t\"width\": " + Float.parseFloat(width) + ",\n" +
                        "\t\"length\": " + Float.parseFloat(length) + ",\n" +
                        "\t\"depth\": " + Float.parseFloat(depth) + "\n" +
                        "}";

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
                //This method POST request and we receive return data in response
                api.postUser(requestBody).enqueue(new Callback<ResponseBody>() {
                    //Method executed if we receive response from server
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            String json = response.body().string();     //receive reponse from server
                            JSONObject jsonOb = new JSONObject(json);
                            double price = Double.parseDouble(jsonOb.optString("price"));
                            if(price>10000000) {
                                String _price = Double.toString(price);
                                int price_index = _price.indexOf('E');
                                double __price = Double.parseDouble(_price.substring(0,price_index));
                                outputTextView.setText("Price: " + String.format("%.2f", __price) + "E" + _price.substring(price_index+1) + "¢");          //Price displayed in scientific notation
                            }
                                else
                                outputTextView.setText("Price: " + String.format("%.2f", price) + "¢");     //Price displayed as double rounded off to two decimal places
                            buttonState="reset";
                            submitButton.setClickable(true);
                            submitButton.setText("Reset");
                        } catch (Exception e) {
                            e.printStackTrace();
                            buttonState="calculator";
                            submitButton.setClickable(true);
                            outputTextView.setText("");
                            lengthEditText.setEnabled(true);
                            widthEditText.setEnabled(true);
                            depthEditText.setEnabled(true);
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
                        lengthEditText.setEnabled(true);
                        widthEditText.setEnabled(true);
                        depthEditText.setEnabled(true);
                    }
                });

            }
        }
    }
}
