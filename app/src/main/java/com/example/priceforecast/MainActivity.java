package com.example.priceforecast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


    public void getData(View view){
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

            if (length.equals("")) {
                lengthEditText.requestFocus();
                Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
            } else if (width.equals("")) {
                widthEditText.requestFocus();
                Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
            } else if (height.equals("")) {
                heightEditText.requestFocus();
                Toast.makeText(this, "Please fill all fields and press calculate", Toast.LENGTH_SHORT).show();
            } else {
                calculatingTextView.setText("Calculating Price");
                submitButton.setClickable(false);
                buttonState="fetching_price";
                lengthEditText.setEnabled(false);
                widthEditText.setEnabled(false);
                heightEditText.setEnabled(false);
                //Retrofit Class
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://prediction1-rest-api.herokuapp.com/")
                        .build();

                Api api = retrofit.create(Api.class);

                String json = "{\n" +
                        "\t\"width\": " + width + ",\n" +
                        "\t\"length\": " + length + ",\n" +
                        "\t\"height\": " + height + "\n" +
                        "}";

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                api.postUser(requestBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        try {
                            calculatingTextView.setText("");
                            String json = response.body().string();
                            JSONObject jsonOb = new JSONObject(json);
                            float price = Float.parseFloat(jsonOb.optString("price"));
                            if(price>10000000)
                                outputTextView.setText("$"+(price));
                            else
                                outputTextView.setText("$" + String.format("%.2f", price));
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

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(t.toString().equals("java.net.UnknownHostException: Unable to resolve host \"prediction1-rest-api.herokuapp.com\": No address associated with hostname"))
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
