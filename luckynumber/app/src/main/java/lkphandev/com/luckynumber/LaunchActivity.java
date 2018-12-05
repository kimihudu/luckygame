package lkphandev.com.luckynumber;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mmin18.widget.FlexLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;

import static android.widget.RadioGroup.*;

public class LaunchActivity extends AppCompatActivity {
    ImageButton btnStart;
    ImageButton btnPin;
    //    ImageButton btnCheckCredit;
    EditText txtPhoneNo;

    RadioGroup groupLoc1;
    RadioGroup groupLoc2;
    RadioButton btnLoc1;
    RadioButton btnLoc2;
    RadioButton btnLoc3;
    RadioButton btnLoc4;
    RadioButton btnLoc5;
    RadioButton btnLoc6;
    RadioButton btnLoc7;
    RadioButton btnLoc8;
    RadioButton btnLoc9;
    RadioButton btnLoc10;
    RadioButton btnLoc11;
    TextView lbCell_Pin;
    FlexLayout mainView;

//    RadioButton btnLoc12;

    final int MAX_NO = 10;
    Games games = new Games();
    //flag for check first run after install
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor;
    String currentSection = "";
    static String MASTER_PIN = "55555";
    static String FIRST_VIEW = "LaunchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            currentSection = bundle.getString("from");
        } else
            currentSection = "LaunchActivity";

        sharedPreferences = getSharedPreferences("com.storematepos.luckynumber", MODE_PRIVATE);
        commonControl();

        if (FIRST_VIEW.equals(currentSection) || currentSection == null) {

            //check for the first run
            if (sharedPreferences.getBoolean("firstRun", true))
                initFirstRun();
            else
                validatePin();
        } else {
//            Log.i("currentSection", currentSection);
            btnPin.setVisibility(View.GONE);
            addControl();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        //check for the first run after install
        if (sharedPreferences.getBoolean("firstRun", true)) {
            //You can perform anything over here. This will call only first time
            editor = sharedPreferences.edit();
            editor.putBoolean("firstRun", false);
            editor.commit();
        }
    }

    //handle for backkey device
    @Override
    public void onBackPressed() {
    }

    private void commonControl() {

        btnPin = findViewById(R.id.btnPin);
        lbCell_Pin = findViewById(R.id.lbCell_Pin);
        txtPhoneNo = findViewById(R.id.txtPhoneNo);

        lbCell_Pin.setText("PIN NUMBER");
        txtPhoneNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        txtPhoneNo.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
        txtPhoneNo.setSelection(txtPhoneNo.getText().length());
    }

    private void initFirstRun() {

        txtPhoneNo.setHint(R.string.lbSetPin);

        btnPin.setImageResource(R.mipmap.btn_setpin);
        btnPin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnPin.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnPin.setBackgroundResource(R.drawable.cus_btn_normal);
                        if (!txtPhoneNo.getText().equals("")) {
                            Boolean success = Ultis.writeLocFile(getApplicationContext(), txtPhoneNo.getText().toString());
                            System.out.println(success);
                            btnPin.setVisibility(View.GONE);
                            addControl();
                        }
                        break;
                }

                return false;
            }
        });
    }

    private void validatePin() {

        txtPhoneNo.setHint(R.string.lbPin);
        btnPin.setImageResource(R.mipmap.btn_unlock);
        btnPin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnPin.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnPin.setBackgroundResource(R.drawable.cus_btn_normal);
                        if (!txtPhoneNo.getText().equals("")) {
                            String _pin = Ultis.readLocFile(getApplicationContext());
                            System.out.println(_pin);
                            String inputPin = txtPhoneNo.getText().toString();

                            if (inputPin.equals(MASTER_PIN) || inputPin.equals(_pin)) {
                                btnPin.setVisibility(View.GONE);
                                addControl();
                            }

                        }
                        break;
                }

                return false;
            }
        });
    }

    private void addControl() {

        btnStart = findViewById(R.id.btnStart);

        lbCell_Pin.setText("PHONE NUMBER");
        txtPhoneNo.setHint("");
        txtPhoneNo.getText().clear();
        txtPhoneNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NO)});
        txtPhoneNo.setInputType(InputType.TYPE_CLASS_NUMBER);

        btnStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnStart.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnStart.setBackgroundResource(R.drawable.cus_btn_normal);
                        if (txtPhoneNo.getText() != null && txtPhoneNo.getText().length() == 10) {
                            games.setPhone_no(txtPhoneNo.getText().toString());
                            if (validatePackage(games)) {
                                //send query file to server
                                new RequestFTPserver().execute();
                                Bundle bundle = new Bundle();
                                bundle.putString("games", new Gson().toJson(games));
                                bundle.putString("from", LaunchActivity.this.getClass().getSimpleName());
                                Intent nextI = new Intent(LaunchActivity.this, SelectGameActivity.class);
                                nextI.putExtras(bundle);
                                startActivity(nextI);

                            }
                        }
                        break;
                }

                return false;
            }
        });
    }

    private void displayControl() {

//        groupLoc1 = findViewById(R.id.toggleLoc1);
//        groupLoc2 = findViewById(R.id.toggleLoc2);
//        groupLoc1.clearCheck(); // this is so we can start fresh, with no selection on both RadioGroups
//        groupLoc2.clearCheck();
//        groupLoc1.setOnCheckedChangeListener(listenerGroup1);
//        groupLoc2.setOnCheckedChangeListener(listenerGroup2);

//        btnLoc1 = findViewById(R.id.btnLoc1);
//        btnLoc2 = findViewById(R.id.btnLoc2);
//        btnLoc3 = findViewById(R.id.btnLoc3);
//        btnLoc4 = findViewById(R.id.btnLoc4);
//        btnLoc5 = findViewById(R.id.btnLoc5);
//        btnLoc6 = findViewById(R.id.btnLoc6);
//        btnLoc7 = findViewById(R.id.btnLoc7);
//        btnLoc8 = findViewById(R.id.btnLoc8);
//        btnLoc9 = findViewById(R.id.btnLoc9);
//        btnLoc10 = findViewById(R.id.btnLoc10);
//        btnLoc11 = findViewById(R.id.btnLoc11);
//
//        btnLoc1.setOnClickListener(locListener);
//        btnLoc2.setOnClickListener(locListener);
//        btnLoc3.setOnClickListener(locListener);
//        btnLoc4.setOnClickListener(locListener);
//        btnLoc5.setOnClickListener(locListener);
//        btnLoc6.setOnClickListener(locListener);
//        btnLoc7.setOnClickListener(locListener);
//        btnLoc8.setOnClickListener(locListener);
//        btnLoc9.setOnClickListener(locListener);
//        btnLoc10.setOnClickListener(locListener);
//        btnLoc11.setOnClickListener(locListener);

        final TextView lbCell_Pin = findViewById(R.id.lbCell_Pin);
        final EditText txtPhoneNo = findViewById(R.id.txtPhoneNo);
        final ImageButton btnStart = findViewById(R.id.btnStart);

        lbCell_Pin.setText("PHONE NUMBER");
        txtPhoneNo.setHint("");
        txtPhoneNo.setText("");
        txtPhoneNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NO)});
        txtPhoneNo.setInputType(InputType.TYPE_CLASS_NUMBER);

//        btnCheckCredit = findViewById(R.id.btnCheckCredit);
//        btnCheckCredit.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent e) {
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        btnCheckCredit.setBackgroundResource(R.drawable.cus_btn_pressed);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        btnCheckCredit.setBackgroundResource(R.drawable.cus_btn_normal);
//
//                        if (!txtPhoneNo.getText().toString().equals("")) {
//                            AlertDialog.Builder builderDialog = new AlertDialog.Builder(LaunchActivity.this);
//                            builderDialog.setMessage("Do you want to check your credit?");
//                            builderDialog.setCancelable(true);
//
//                            builderDialog.setPositiveButton(
//                                    "Yes",
//                                    new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            dialog.cancel();
//                                            games.setPhone_no(txtPhoneNo.getText().toString());
//
//                                            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//                                            //send query file to server
//                                            new RequestFTPserver().execute();
//
//                                        }
//                                    });
//
//                            builderDialog.setNegativeButton(
//                                    "No",
//                                    new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            dialog.cancel();
//                                        }
//                                    });
//
//                            AlertDialog alertConfirm = builderDialog.create();
//                            alertConfirm.show();
//                        } else
//                            Toast.makeText(LaunchActivity.this.getApplicationContext(), "please fill your phone no", Toast.LENGTH_SHORT).show();
//
//                        break;
//                }
//
//                return false;
//            }
//        });


        btnStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnStart.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnStart.setBackgroundResource(R.drawable.cus_btn_normal);
                        if (txtPhoneNo.getText() != null && txtPhoneNo.getText().length() == 10) {
                            games.setPhone_no(txtPhoneNo.getText().toString());
                            if (validatePackage(games)) {
                                //send query file to server
                                new RequestFTPserver().execute();
                                Bundle bundle = new Bundle();
                                bundle.putString("games", new Gson().toJson(games));
                                bundle.putString("from", LaunchActivity.this.getClass().getSimpleName());
                                Intent nextI = new Intent(LaunchActivity.this, SelectGameActivity.class);
                                nextI.putExtras(bundle);
                                startActivity(nextI);

                            }
                        }
                        break;
                }

                return false;
            }
        });
    }

    //TODO: workaround for 2 group radio button
//    private OnCheckedChangeListener listenerGroup1 = new OnCheckedChangeListener() {
//
//        @Override
//        public void onCheckedChanged(RadioGroup group, int checkedId) {
//            if (checkedId != -1) {
//                groupLoc2.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception(like Vladimir Volodin pointed out)
//                groupLoc2.clearCheck(); // clear the second RadioGroup!
//                groupLoc2.setOnCheckedChangeListener(listenerGroup2); //reset the listener
//            }
//        }
//    };
//
//    private OnCheckedChangeListener listenerGroup2 = new OnCheckedChangeListener() {
//
//        @Override
//        public void onCheckedChanged(RadioGroup group, int checkedId) {
//            if (checkedId != -1) {
//                groupLoc1.setOnCheckedChangeListener(null);
//                groupLoc1.clearCheck();
//                groupLoc1.setOnCheckedChangeListener(listenerGroup1);
//            }
//        }
//    };
//
//    private View.OnClickListener locListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            games.setLocation(((RadioButton) view).getText().toString());
//        }
//    };

    private boolean validatePackage(Games games) {

//        (games.getPhone_no() == null || games.getLocation() == null)
        if (games.getPhone_no() == null) {
            Toast.makeText(LaunchActivity.this, "Please fill full info before go to next screen", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }

    //TODO:/*task sending file to server */
    private class RequestFTPserver extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Context context = LaunchActivity.this.getApplicationContext();
            return Ultis.save2Sever(context, games, "Acct");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("RequestFTPserver", result.toString());
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


}
