package lkphandev.com.luckynumber;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputFilter;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mmin18.widget.FlexLayout;
import com.google.gson.Gson;

public class PaymentActivity extends AppCompatActivity {

    ImageButton btnBack;
    ImageButton btnPoint;
    ImageButton btnCredit;
    ImageButton btnNewGame;
    View popup_payment_credit;
    TextView lbTotal;
    TextView lbCredit;
    TextView lbPoint;

    //global var
    Games games;
    PopupWindow popupWindow = null;
    static int WAIT_MID = 1000;

    //handle for hardware back button do nothing
    @Override
    public void onBackPressed() {
    }

    //handle for hardware back button for popup window
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Override back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (popupWindow != null) {
                if (popupWindow.isShowing())
                    popupWindow.dismiss();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            games = new Gson().fromJson(bundle.getString("games"), Games.class);
        }
        addControl();
    }

    private void addControl() {
        btnBack = findViewById(R.id.btnBack);
        btnPoint = findViewById(R.id.btnPoint);
        btnCredit = findViewById(R.id.btnCredit);
        btnNewGame = findViewById(R.id.btnNewGame);
        lbCredit = findViewById(R.id.lbCredit);
        lbPoint = findViewById(R.id.lbPoint);
        lbTotal = findViewById(R.id.lbTotal);

        lbTotal.setText(games.getTotals());
        lbCredit.setText(games.getCreditBal());
        lbPoint.setText(games.getPointBal());

        btnNewGame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnNewGame.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnNewGame.setBackgroundResource(R.drawable.cus_btn_normal);
                        newGame();
                        break;
                }
                return false;
            }
        });

        btnBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnBack.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnBack.setBackgroundResource(R.drawable.cus_btn_normal);
                        Bundle bundle = new Bundle();
                        bundle.putString("from", PaymentActivity.this.getClass().getSimpleName());
                        bundle.putString("games", new Gson().toJson(games));
                        Intent prevI = new Intent(PaymentActivity.this, SelectGameActivity.class);
                        prevI.putExtras(bundle);
                        startActivity(prevI);
                        break;
                }
                return false;
            }
        });

        btnCredit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnCredit.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnCredit.setBackgroundResource(R.drawable.cus_btn_normal);
                        if (!validateAmount("credit")) {
                            Toast.makeText(getApplicationContext(), "please check your account balance", Toast.LENGTH_SHORT).show();
                        } else {
                            popCreditPay();
                        }


                        //send query file to server
//                        new RequestFTPserver().execute();
//                        newGame();
                        break;
                }
                return false;
            }
        });

        btnPoint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnPoint.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnPoint.setBackgroundResource(R.drawable.cus_btn_normal);

                        if (!validateAmount("point")) {
                            Toast.makeText(getApplicationContext(), "please check your account balance", Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder builderDialog = new AlertDialog.Builder(PaymentActivity.this);
                            builderDialog.setMessage("Are you sure");
                            builderDialog.setCancelable(true);

                            builderDialog.setPositiveButton(
                                    "Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            games.setPaypal("Point");
                                            //send query file to server
                                            new RequestFTPserver().execute(games);

                                        }
                                    });

                            builderDialog.setNegativeButton(
                                    "No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alertConfirm = builderDialog.create();
                            alertConfirm.show();
                        }
                        break;
                }
                return false;
            }
        });

    }


    private void newGame() {
        Bundle bundle = new Bundle();
        //reset game
        games.getGames().clear();
        bundle.putString("from", PaymentActivity.this.getClass().getSimpleName());
        bundle.putString("games", new Gson().toJson(games));
        Intent prevI = new Intent(PaymentActivity.this, SelectGameActivity.class);
        prevI.putExtras(bundle);
        startActivity(prevI);
    }

    private boolean validatePackage(Games games) {
        if (games.getPhone_no() == null || games.getLocation() == null) {
            Toast.makeText(PaymentActivity.this, "Please fill full info before go to next screen", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }


    private void popCreditPay() {

        final int MAX_NO = 5;
        //popup add money
        LayoutInflater layoutInflater = (LayoutInflater) PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popup_payment_credit = layoutInflater.inflate(R.layout.pop_pay_credit, null);


        ImageButton pop_btnMoreMoney = popup_payment_credit.findViewById(R.id.btnMoreMoney);
        ImageButton pop_btnSavePoint = popup_payment_credit.findViewById(R.id.btnSavePoint);
        ImageButton pop_btnBack = popup_payment_credit.findViewById(R.id.btnGoBack);


        //instantiate popup window
        popupWindow = new PopupWindow(popup_payment_credit, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);


        //add blur for background
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        Ultis.applyDim(root, 0.8f); //or clearDim()

        //get parent size and set size to popup
        WindowManager wm = (WindowManager) PaymentActivity.this.getSystemService(Context.WINDOW_SERVICE);
        double measuredWidth;
        double measuredHeight;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            measuredWidth = size.x * 0.8;
            measuredHeight = size.y * 0.5;
        } else {
            Display d = wm.getDefaultDisplay();
            measuredWidth = d.getWidth();
            measuredHeight = d.getHeight();
        }

        //set size for popup
        popupWindow.setWidth((int) measuredWidth);
        popupWindow.setHeight((int) measuredHeight);


        //set focus for keyboard
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.update();
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);

        // OverWrite dismiss with include clear dim and softkey
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Ultis.clearDim(root);
                popupWindow.dismiss();
            }
        });

        pop_btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        pop_btnMoreMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                games.setPaypal("credit_more_money");
                popupWindow.dismiss();
                new RequestFTPserver().execute(games);

            }
        });

        pop_btnSavePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                games.setPaypal("credit_save_point");
                popupWindow.dismiss();
                new RequestFTPserver().execute(games);

            }
        });
    }

    /*task sending file to server */
    private class RequestFTPserver extends AsyncTask<Games, Void, Boolean> {
        AlertDialog.Builder builderDialog = new AlertDialog.Builder(PaymentActivity.this);

        @Override
        protected Boolean doInBackground(Games... games) {
            Context context = PaymentActivity.this.getApplicationContext();
            return Ultis.save2Sever(context, games[0], "Ticket");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("RequestFTPserver", result.toString());
            if (result) {
                builderDialog.setMessage("Your tickets is submitted");
//                builderDialog.setCancelable(true);

                builderDialog.setPositiveButton(
                        "ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                Handler handler = new Handler();
                                Runnable waitForNGame = new Runnable() {
                                    @Override
                                    public void run() {
                                        newGame();
                                    }
                                };
                                handler.postDelayed(waitForNGame,WAIT_MID);
                            }
                        });

//                builderDialog.setNegativeButton(
//                        "No",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });

                AlertDialog alertConfirm = builderDialog.create();
                alertConfirm.show();

            } else
                Toast.makeText(getApplicationContext(), "connection server fail...", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private Boolean validateAmount(String accType) {
//        int _newBid = Integer.parseInt(newBid);
        int amount = Integer.parseInt(games.getTotals());
        int credit = Integer.parseInt(games.getCreditBal());
        int point = Integer.parseInt(games.getPointBal());

        switch (accType) {
            case "credit":
                if (amount > credit)
                    return false;
                break;
            case "point":
                if (amount > point)
                    return false;
                break;
        }
        return true;
    }
}
