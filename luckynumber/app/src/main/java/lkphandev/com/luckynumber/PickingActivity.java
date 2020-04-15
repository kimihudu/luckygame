package lkphandev.com.luckynumber;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mmin18.widget.FlexLayout;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PickingActivity extends AppCompatActivity {

    //Control
    ImageButton btnBack;
    ImageButton btnView;
//    ImageButton btnDelete;

    //for C1->C4
    RadioButton btnC1;
    RadioButton btnC2;
    RadioButton btnC3;
    RadioButton btnC4;
    RadioButton btnB;
    RadioButton btnJ;
    RadioGroup toggleSubG;

    //for C5->C6
    RadioButton btnGT1;
    RadioButton btnC3B;
    RadioButton btnC4B;

    EditText txtPickedNo;
    View popup_addMoney;
    FlexLayout mainView;
    TextView lbLastPicked;
    TextView lbGameType;
    TextView lbCredit;
    TextView lbPoint;
    TextView lbTotal;

    //global var
    String phoneNo;
    String gameName;
    String usrGameName;
    String pickedNo;
    String gameType;
    String amountMoney;
    String creditBal = "0";
    String pointBal = "0";
    String totalMoney = "0";
    Games games;
    Game game;
    ViewGroup root = null;
    PopupWindow popupWindow = null;
    InputMethodManager imm = null;

    //Global variable
    final int WAIT_RANDOM_NO = 300;
    int MAX_NO = 0;
    final String TEXT_G6 = "_-_-_";
    final String TEXT_G5 = "_-_-_-_";
    int numberOfBtnNo = 0;
    final String QUICK_PICK = "Quick Pick";

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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String selectPck = bundle.getString("from");
            games = new Gson().fromJson(bundle.getString("games"), Games.class);
            int latestGame = games.getGames().size();
            game = games.getGames().get(latestGame - 1);
            phoneNo = games.getPhone_no();
            gameName = game.getGame_name();

            if (!games.getCreditBal().equals(""))
                creditBal = games.getCreditBal();
            if (!games.getPointBal().equals(""))
                pointBal = games.getPointBal();
            if (!games.getTotals().equals(""))
                totalMoney = games.getTotals();

        }

        //TODO: select layout for game
        switch (gameName) {
            case "G3":
                setContentView(R.layout.activity_picking_g4);
                numberOfBtnNo = 40; //--> update 3 march
                MAX_NO = 23;
                controlG1toG4();
                break;
            case "G1":
                setContentView(R.layout.activity_picking_g1);
                numberOfBtnNo = 51; //--> 011320lk - update lotte 50
                MAX_NO = 23;
                controlG1toG4();
                break;
            case "G2":
                setContentView(R.layout.activity_picking_g2);
                numberOfBtnNo = 50; //--> update 3 march
                MAX_NO = 23;
                controlG1toG4();
                break;
            case "G4":
                setContentView(R.layout.activity_picking);
                numberOfBtnNo = 50; //--> update 3 march
                MAX_NO = 23;
                controlG1toG4();
                break;
            case "G5":
                setContentView(R.layout.activity_picking_g5);
                numberOfBtnNo = 40;
                MAX_NO = 7;
                btnGT1 = findViewById(R.id.btnGT1);
                btnC4B = findViewById(R.id.btnC4B);
                btnGT1.setOnClickListener(typeGameListener);
                btnC4B.setOnClickListener(typeGameListener);
                break;
            case "G6":
                setContentView(R.layout.activity_picking_g6);
                numberOfBtnNo = 30;
                MAX_NO = 6;
                btnGT1 = findViewById(R.id.btnGT1);
                btnC3B = findViewById(R.id.btnC3B);
                btnGT1.setOnClickListener(typeGameListener);
                btnC3B.setOnClickListener(typeGameListener);
                break;

        }

        commonControl();
        root = (ViewGroup) getWindow().getDecorView().getRootView();
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void controlG1toG4() {

        btnC1 = findViewById(R.id.btnC1);
        btnC2 = findViewById(R.id.btnC2);
        btnC3 = findViewById(R.id.btnC3);
        btnC4 = findViewById(R.id.btnC4);
        btnB = findViewById(R.id.btnB);
//        btnJ = findViewById(R.id.btnJ);

        btnC1.setOnClickListener(typeGameListener);
        btnC2.setOnClickListener(typeGameListener);
        btnC3.setOnClickListener(typeGameListener);
        btnC4.setOnClickListener(typeGameListener);
        btnB.setOnClickListener(typeGameListener);
//        btnJ.setOnClickListener(typeGameListener);

    }

    private void commonControl() {

        toggleSubG = findViewById(R.id.toggleSubG);
        mainView = findViewById(R.id.mainView);
        lbGameType = findViewById(R.id.lb_game_type);
        lbLastPicked = findViewById(R.id.lbLastPicked);
        if (game.getTickets().size() > 0) {
            Ticket lastTicket = game.getTickets().get(game.getTickets().size() - 1);
            lbLastPicked.setText(lastTicket.getGame_no());
        }

        lbCredit = findViewById(R.id.lbCredit);
        lbPoint = findViewById(R.id.lbPoint);
        lbTotal = findViewById(R.id.lbTotal);

        lbCredit.setText(creditBal);
        lbPoint.setText(pointBal);
        lbTotal.setText(calTotalMoney());

        //TODO: tranfer game name backend with frontend
        switch (gameName) {
            case "G1":
                lbGameType.setText(R.string.G1);
                break;
            case "G2":
                lbGameType.setText(R.string.G2);
                break;
            case "G3":
                lbGameType.setText(R.string.G3); // --> update follow require
                break;
            case "G4":
                lbGameType.setText(R.string.G4); // --> update follow require
                break;
            case "G5":
                lbGameType.setText(R.string.G5);
                break;
            case "G6":
                lbGameType.setText(R.string.G6);
                break;
        }


        btnBack = findViewById(R.id.btnBack);
        btnView = findViewById(R.id.btnView);
//        btnDelete = findViewById(R.id.btnDelete);

        txtPickedNo = findViewById(R.id.txtPickedNo);

        //handle for touching in edittext without keyboard displays
        txtPickedNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);   // handle the event first
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  // hide the soft keyboard
                }
                return true;
            }
        });

        switch (gameName) {
            case "G5":
                txtPickedNo.setText(TEXT_G5);
                break;
            case "G6":
                txtPickedNo.setText(TEXT_G6);
                break;
        }

        txtPickedNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NO)});

        btnBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnBack.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnBack.setBackgroundResource(R.drawable.cus_btn_normal);

//                        reset game before turnback choose new one
//                        games.getGames().clear();
                        Bundle bundle = new Bundle();
                        bundle.putString("games", new Gson().toJson(games));
                        Intent prevI = new Intent(PickingActivity.this, SelectGameActivity.class);
                        prevI.putExtras(bundle);
                        startActivity(prevI);
                        break;
                }
                return false;
            }
        });

        btnView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnView.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnView.setBackgroundResource(R.drawable.cus_btn_normal);
                        Bundle bundle = new Bundle();
                        bundle.putString("from", PickingActivity.this.getClass().getSimpleName());
                        bundle.putString("games", new Gson().toJson(games));
                        Intent nextI = new Intent(PickingActivity.this, ViewGamesActivity.class);
                        nextI.putExtras(bundle);
                        startActivity(nextI);
                        break;
                }
                return false;
            }
        });


//        btnDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int length = txtPickedNo.getText().length();
//                String pickedNo = txtPickedNo.getText().toString();
//                int currentSelection = txtPickedNo.getSelectionStart();
//                if (length > 0) {
//
//                    if (currentSelection == MAX_NO)
//                        txtPickedNo.getText().delete(length - 2, length);
//                    else if (currentSelection < length) {
//
//                        if (pickedNo.substring(currentSelection, currentSelection + 1).equals("-"))
//                            txtPickedNo.getText().delete(currentSelection - 2, currentSelection + 1);
//                        else if (pickedNo.substring(currentSelection - 1, currentSelection).equals("-"))
//                            txtPickedNo.getText().delete(currentSelection, currentSelection + 3);
//                        else
//                            txtPickedNo.getText().delete(currentSelection - 1, currentSelection + 2);
//                    } else if (currentSelection == length)
//                        txtPickedNo.getText().delete(length - 3, length);
//
//                }
//
//            }
//        });


        //auto render btnNo follow layout by numberOfBtnNo
        for (int i = 0; i < numberOfBtnNo; i++) {
            int id = getResources().getIdentifier("btn" + i,
                    "id", PickingActivity.this.getApplicationContext().getPackageName());
            Button btni = findViewById(id);
            btni.setOnClickListener(btnNoListener);
        }


    }

    private View.OnClickListener btnNoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            String pickedBtn = ((Button) view).getText().toString();
            String selectedNo;
            String pos;
            String no;

            if (pickedBtn.equals(QUICK_PICK)) {

                resetStateBtnNoByid(0, numberOfBtnNo);
                txtPickedNo.setText(setQUICK_PICK(8, gameName));

                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        setStateBtnNoByid(txtPickedNo.getText().toString());
                        Log.i("current no", txtPickedNo.getText().toString());
                    }
                };
                handler.postDelayed(r, WAIT_RANDOM_NO);


            } else {
                switch (gameName) {
                    case "G1":
                    case "G2":
                    case "G3":
                    case "G4":
                        if (view.getBackground().getConstantState() != getResources().getDrawable(R.drawable.cus_btnno_pressed).getConstantState()) {
                            if (txtPickedNo.getText().length() < MAX_NO) {

                                ((Button) view).setBackgroundResource(R.drawable.cus_btnno_pressed);
                                String tmp = ((Button) view).getText().toString();
                                int start = Math.max(txtPickedNo.getSelectionStart(), 0);
                                int end = Math.max(txtPickedNo.getSelectionEnd(), 0);

                                if (start == 0)
                                    if (txtPickedNo.getText().length() == 0)
                                        txtPickedNo.getText().replace(Math.min(start, end), Math.max(start, end),
                                                tmp, 0, tmp.length());
                                    else
                                        txtPickedNo.getText().replace(Math.min(start, end), Math.max(start, end),
                                                tmp + "-", 0, tmp.length() + 1);
                                else
                                    txtPickedNo.getText().replace(Math.min(start, end), Math.max(start, end),
                                            "-" + tmp, 0, tmp.length() + 1);

                            }

                        } else {
                            //set state
                            ((Button) view).setBackgroundResource(R.drawable.cus_btnno_normal);
                            //delete picked
                            String delText = ((Button) view).getText().toString();
                            int currentSelection = txtPickedNo.getText().toString().indexOf(delText);

                            txtPickedNo.requestFocus();
                            txtPickedNo.setSelection(currentSelection); // --> set pos for cursor wherever delete the text for input the same place next time

                            if (currentSelection == 0)
                                if (delText.length() == txtPickedNo.getText().toString().length())
                                    txtPickedNo.getText().delete(currentSelection, currentSelection + 2);
                                else
                                    txtPickedNo.getText().delete(currentSelection, currentSelection + 3);
                            else
                                txtPickedNo.getText().delete(currentSelection - 1, currentSelection + 2);
                        }
                        break;

                    case "G5":

                        selectedNo = txtG5(id);
                        pos = selectedNo.substring(0, 2);
                        no = selectedNo.substring(2);
                        resetStateBtnNoByCol(pos);
                        ((Button) view).setBackgroundResource(R.drawable.cus_btnno_pressed);

                        if (pos.equals("c1"))

                            txtPickedNo.getText().replace(Math.min(0, 1), Math.max(0, 1),
                                    no, 0, no.length());
                        else if (pos.equals("c2"))
                            txtPickedNo.getText().replace(Math.min(2, 3), Math.max(2, 3),
                                    no, 0, no.length());
                        else if (pos.equals("c3"))
                            txtPickedNo.getText().replace(Math.min(4, 5), Math.max(4, 5),
                                    no, 0, no.length());
                        else if (pos.equals("c4"))
                            txtPickedNo.getText().replace(Math.min(6, 7), Math.max(6, 7),
                                    no, 0, no.length());
                        break;

                    case "G6":

                        selectedNo = txtG6(id);
                        pos = selectedNo.substring(0, 2);
                        no = selectedNo.substring(2);
                        resetStateBtnNoByCol(pos);
                        ((Button) view).setBackgroundResource(R.drawable.cus_btnno_pressed);

                        if (pos.equals("c1"))
                            txtPickedNo.getText().replace(Math.min(0, 1), Math.max(0, 1),
                                    no, 0, no.length());
                        else if (pos.equals("c2"))
                            txtPickedNo.getText().replace(Math.min(2, 3), Math.max(2, 3),
                                    no, 0, no.length());
                        else if (pos.equals("c3"))
                            txtPickedNo.getText().replace(Math.min(4, 5), Math.max(4, 5),
                                    no, 0, no.length());
                        break;

                }
            }

        }
    };

    private View.OnClickListener typeGameListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String type = ((RadioButton) view).getText().toString();
            switch (view.getId()) {
                case R.id.btnC3B:
                    type = "C3B";
                    break;
                case R.id.btnC4B:
                    type = "C4B";
                    break;
            }

            gameType = type;
            if (validateGameType(txtPickedNo.getText().toString(), type)) {
                pickedNo = txtPickedNo.getText().toString();
                popAddMoney(mainView);
            } else
                Toast.makeText(getApplicationContext(), "please pick correct game type", Toast.LENGTH_LONG).show();

        }
    };

    private void popAddMoney(View v) {

        int _MAX_NO = 5;
        //popup add money
        LayoutInflater layoutInflater = (LayoutInflater) PickingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popup_addMoney = layoutInflater.inflate(R.layout.pop_amount_money, null);

        final ImageButton btnAdd = popup_addMoney.findViewById(R.id.btnAdd);
        final ImageButton btnCancel = popup_addMoney.findViewById(R.id.btnCancel);
        final EditText txtAmountMoney = popup_addMoney.findViewById(R.id.txtAmount);
        txtAmountMoney.setFilters(new InputFilter[]{new InputFilter.LengthFilter(_MAX_NO)});

        //instantiate popup window
        popupWindow = new PopupWindow(popup_addMoney, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);


        //get parent size and set size to popup
        WindowManager wm = (WindowManager) PickingActivity.this.getSystemService(Context.WINDOW_SERVICE);
        double measuredWidth;
        double measuredHeight;

        //add blur for background
        Ultis.applyDim(root, 0.8f); //or clearDim()

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

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true); //set focus for get effected from keyboard
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        txtAmountMoney.requestFocus();

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);// --> auto show softkey

        // OverWrite dismiss with include clear dim and softkey
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Ultis.clearDim(root);
                toggleSubG.clearCheck();
                popupWindow.dismiss();
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        btnCancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnCancel.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnCancel.setBackgroundResource(R.drawable.cus_btn_normal);
                        popupWindow.dismiss();
//                        Ultis.clearDim(root);
//                        toggleSubG.clearCheck();
//                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        break;
                }
                return false;
            }
        });

        btnAdd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnAdd.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnAdd.setBackgroundResource(R.drawable.cus_btn_normal);
                        if (!txtAmountMoney.getText().toString().equals("")) {
                            amountMoney = txtAmountMoney.getText().toString();
                            if (!validateAmount(txtAmountMoney.getText().toString())) {
                                Toast.makeText(getApplicationContext(), "please check your Account balance", Toast.LENGTH_SHORT).show();
                            } else {
                                addTicket();
                                txtPickedNo.getText().clear();
                                switch (gameName) {
                                    case "G5":
                                        txtPickedNo.setText(TEXT_G5);
                                        break;
                                    case "G6":
                                        txtPickedNo.setText(TEXT_G6);
                                        break;
                                }
                                resetAllState();
                                popupWindow.dismiss();
                            }

                        } else
                            Toast.makeText(getApplicationContext(), "please enter amount of money", Toast.LENGTH_SHORT).show();

                        break;
                }
                return false;
            }
        });
    }

    //TODO: add ticket for a game
    private void addTicket() {

        boolean no = Ultis.selfCensor(PickingActivity.this, pickedNo);
        boolean type = Ultis.selfCensor(PickingActivity.this, gameType);
        boolean money = Ultis.selfCensor(PickingActivity.this, amountMoney);
        Ticket ticket = new Ticket();

        if (no)
            ticket.setGame_no(pickedNo);

        if (type)
            ticket.setGame_type(gameType);

        if (money)
            ticket.setAmount(amountMoney);

        if (no && type && money){
            String _id = String.valueOf(game.getTickets().size() + 1) ;
            ticket.setId(_id);
            game.getTickets().add(ticket);
        }

        lbTotal.setText(calTotalMoney());
        lbLastPicked.setText(ticket.getGame_no());

    }

    private String txtG6(int id) {

        String col1Text = "c1";
        String col2Text = "c2";
        String col3Text = "c3";

        switch (id) {
            case R.id.btn0:
            case R.id.btn1:
            case R.id.btn2:
            case R.id.btn3:
            case R.id.btn4:
            case R.id.btn5:
            case R.id.btn6:
            case R.id.btn7:
            case R.id.btn8:
            case R.id.btn9:
                col1Text += ((Button) findViewById(id)).getText().toString();
                return col1Text;
            case R.id.btn10:
            case R.id.btn11:
            case R.id.btn12:
            case R.id.btn13:
            case R.id.btn14:
            case R.id.btn15:
            case R.id.btn16:
            case R.id.btn17:
            case R.id.btn18:
            case R.id.btn19:
                col2Text += ((Button) findViewById(id)).getText().toString();
                return col2Text;
            case R.id.btn20:
            case R.id.btn21:
            case R.id.btn22:
            case R.id.btn23:
            case R.id.btn24:
            case R.id.btn25:
            case R.id.btn26:
            case R.id.btn27:
            case R.id.btn28:
            case R.id.btn29:
                col3Text += ((Button) findViewById(id)).getText().toString();
                return col3Text;
        }
        return null;

    }

    private String txtG5(int id) {

        String col1Text = "c1";
        String col2Text = "c2";
        String col3Text = "c3";
        String col4Text = "c4";

        switch (id) {
            case R.id.btn0:
            case R.id.btn1:
            case R.id.btn2:
            case R.id.btn3:
            case R.id.btn4:
            case R.id.btn5:
            case R.id.btn6:
            case R.id.btn7:
            case R.id.btn8:
            case R.id.btn9:
                col1Text += ((Button) findViewById(id)).getText().toString();
                return col1Text;
            case R.id.btn10:
            case R.id.btn11:
            case R.id.btn12:
            case R.id.btn13:
            case R.id.btn14:
            case R.id.btn15:
            case R.id.btn16:
            case R.id.btn17:
            case R.id.btn18:
            case R.id.btn19:
                col2Text += ((Button) findViewById(id)).getText().toString();
                return col2Text;
            case R.id.btn20:
            case R.id.btn21:
            case R.id.btn22:
            case R.id.btn23:
            case R.id.btn24:
            case R.id.btn25:
            case R.id.btn26:
            case R.id.btn27:
            case R.id.btn28:
            case R.id.btn29:
                col3Text += ((Button) findViewById(id)).getText().toString();
                return col3Text;
            case R.id.btn30:
            case R.id.btn31:
            case R.id.btn32:
            case R.id.btn33:
            case R.id.btn34:
            case R.id.btn35:
            case R.id.btn36:
            case R.id.btn37:
            case R.id.btn38:
            case R.id.btn39:
                col4Text += ((Button) findViewById(id)).getText().toString();
                return col4Text;
        }
        return null;

    }

    private String setQUICK_PICK(int noOfPairRandom, String gameName) {

        //random noOfPair
//        Random r = new Random();
//        int max = 8;
//        int min = 1;
//        int randomTimes = r.nextInt((max - min) + 1) + min;

        String quickPick = "";

        switch (noOfPairRandom) {
            case 1:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(1,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(1);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(1, 1, 39);
                        break;
                }

                break;
            case 2:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(2,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(2);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(2, 1, 39);
                        break;
                }

                break;
            case 3:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(3,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(3);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(3, 1, 39);
                        break;
                }
                break;
            case 4:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(4,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(4);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(4, 1, 39);
                        break;
                }
                break;
            case 5:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(5,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(5);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(5, 1, 39);
                        break;
                }

                break;
            case 6:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(6,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(6);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(6, 1, 39);
                        break;
                }
                break;
            case 7:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(7,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(7);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(7, 1, 39);
                        break;
                }
                break;
            case 8:
                switch (gameName) {
                    //011320lk - add 50 for lucky Max
                    case "G1":
                        quickPick = Ultis.getRandomNumberInTimes(8,1,50);
                        break;
                    case "G2":
                    case "G4":
                        quickPick = Ultis.getRandomNumberInTimes(8);
                        break;
                    case "G3":
                        quickPick = Ultis.getRandomNumberInTimes(8, 1, 39);
                        break;
                }
                break;
        }

        return quickPick;
    }


    private boolean validateGameType(String checkStr, String validation) {
        Boolean result = false;
        switch (validation) {
            case "C1":
                if (checkStr.length() >= 2)
                    result = true;
                break;
            case "C2":
                if (checkStr.length() >= 5)
                    result = true;
                break;
            case "C3":
                if (gameName.equals("G6")) {
                    if ((checkStr.length() >= 5) && (checkStr.indexOf("_", 0) == -1))
                        result = true;
                } else {
                    if (checkStr.length() >= 8)
                        result = true;
                }
                break;
            case "C3B":
                if ((checkStr.length() >= 5) && (checkStr.indexOf("_", 0) == -1))
                    result = true;
                break;
            case "C4":
                if (gameName.equals("G5")) {
                    if ((checkStr.length() >= 7) && (checkStr.indexOf("_", 0) == -1))
                        result = true;
                } else {
                    if (checkStr.length() >= 11)
                        result = true;
                }

                break;
            case "C4B":
                if ((checkStr.length() >= 7) && (checkStr.indexOf("_", 0) == -1))
                    result = true;
                break;
            case "BO":
                if (checkStr.length() >= 2)
                    result = true;
                break;
        }

        return result;
    }

    private void resetStateBtnNoByCol(String colName) {
        switch (colName) {
            case "c1":
                resetStateBtnNoByid(0, 10);
                break;
            case "c2":
                resetStateBtnNoByid(10, 20);
                break;
            case "c3":
                resetStateBtnNoByid(20, 30);
                break;
            case "c4":
                resetStateBtnNoByid(30, 40);
                break;
        }
    }

    private void resetStateBtnNoByid(int _from, int _to) {
        for (int i = _from; i < _to; i++) {
            int id = getResources().getIdentifier("btn" + i,
                    "id", PickingActivity.this.getApplicationContext().getPackageName());
            Button btni = findViewById(id);
            btni.setBackgroundResource(R.drawable.cus_btnno_normal);
        }
    }

    private void setStateBtnNoByid(String listBtnNo) {

        ArrayList list = Ultis.str2PaiArray(listBtnNo, "-");
        for (Object _id : list) {
            int id = getResources().getIdentifier("btn" + String.format("%01d", (int) _id),
                    "id", PickingActivity.this.getApplicationContext().getPackageName());
            Button btni = findViewById(id);
            btni.setBackgroundResource(R.drawable.cus_btnno_pressed);
        }
    }

    private void resetAllState() {
        //auto render btnNo follow layout by numberOfBtnNo
        for (int i = 0; i < numberOfBtnNo; i++) {
            int id = getResources().getIdentifier("btn" + i,
                    "id", PickingActivity.this.getApplicationContext().getPackageName());
            Button btni = findViewById(id);
            btni.setBackgroundResource(R.drawable.cus_btnno_normal);
        }

        toggleSubG.clearCheck();
    }

    private String calTotalMoney() {
        String total = "";
        games.calculateMoney();
        total = games.getTotals();
        return total;
    }

    private Boolean validateAmount(String moneyBid) {

        try{
            int _moneyBid = Integer.parseInt(moneyBid);
            int amount = Integer.parseInt(games.getTotals());
            int credit = Integer.parseInt(games.getCreditBal());
            int point = Integer.parseInt(games.getPointBal());

            if ((amount + _moneyBid) > credit && (amount + _moneyBid) > point)
                return false;
            return true;

        }catch(Exception e){
            Log.e("validateAmount",e.getMessage());
        }

        return false;
    }

}
