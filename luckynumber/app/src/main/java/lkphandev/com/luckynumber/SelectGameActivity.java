package lkphandev.com.luckynumber;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mmin18.widget.FlexLayout;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class SelectGameActivity extends AppCompatActivity {
    ImageButton btnG1;
    ImageButton btnG2;
    ImageButton btnG3;
    ImageButton btnG4;
    ImageButton btnG5;
    ImageButton btnG6;
    ImageButton btnBack;
    ImageButton btnReplay;
    PopupWindow popupWindow;
    FlexLayout mainView;

    //global var
    Games games = null;
    static int WAIT_TIME = 100;
    String currentSection = "SelectGameActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_game);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            games = new Gson().fromJson(bundle.getString("games"), Games.class);
            if (bundle.get("from") != null)
                currentSection = bundle.getString("from");
        }

        if (currentSection.equals("LaunchActivity") || currentSection.equals("PaymentActivity")) {
            //query accBal
            //after success query acc bal --> addControl()
            new ReqFTPserver_AccBal().execute();

            mainView = findViewById(R.id.mainView);
            //add waiting time for poploading
            Handler handler = new Handler();
            Runnable waitMainView = new Runnable() {
                @Override
                public void run() {
                    popupWindow = Ultis.popLoading(SelectGameActivity.this, mainView);
                }
            };
            handler.postDelayed(waitMainView, WAIT_TIME);
        } else
            addControl();

        btnBack = findViewById(R.id.btnBack);
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
                        bundle.putString("from","SelectGameActivity");
                        Intent prevI = new Intent(SelectGameActivity.this, LaunchActivity.class);
                        prevI.putExtras(bundle);
                        startActivity(prevI);
                        break;
                }
                return false;
            }
        });

    }

    //handle for hardware back button do nothing
    @Override
    public void onBackPressed() {
    }

    private void addControl() {
        btnG1 = findViewById(R.id.btnG1);
        btnG2 = findViewById(R.id.btnG2);
        btnG3 = findViewById(R.id.btnG3);
        btnG4 = findViewById(R.id.btnG4);
        btnG5 = findViewById(R.id.btnG5);
        btnG6 = findViewById(R.id.btnG6);
        btnReplay = findViewById(R.id.btnReplay);


        btnReplay.setOnTouchListener(reqRepTicket);
        btnG1.setOnTouchListener(gameListener);
        btnG2.setOnTouchListener(gameListener);
        btnG3.setOnTouchListener(gameListener);
        btnG4.setOnTouchListener(gameListener);
        btnG5.setOnTouchListener(gameListener);
        btnG6.setOnTouchListener(gameListener);

    }


    View.OnTouchListener reqRepTicket = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setBackgroundResource(R.drawable.cus_btn_pressed);
                    break;
                case MotionEvent.ACTION_UP:

                    view.setBackgroundResource(R.drawable.cus_btn_normal);
                    popReplayTicket();
                    break;
            }
            return false;
        }

    };

    View.OnTouchListener gameListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setBackgroundResource(R.drawable.cus_btn_pressed);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackgroundResource(R.drawable.cus_btn_normal);
                    String gameName = "";
                    switch (view.getId()) {
                        case R.id.btnG1:
                            gameName = "G1";
                            break;
                        case R.id.btnG2:
                            gameName = "G2";
                            break;
                        case R.id.btnG3:
                            gameName = "G3"; // --> update follow require
                            break;
                        case R.id.btnG4:
                            gameName = "G4"; // --> update follow require
                            break;
                        case R.id.btnG5:
                            gameName = "G5";
                            break;
                        case R.id.btnG6:
                            gameName = "G6";
                            break;
                    }

                    if (games.getGames().size() > 0) {
                        boolean dup = false;
                        Game lastestSelectedG = null;
                        for (Game _game : games.getGames()) {
                            if (_game.getGame_name().equals(gameName)) {
                                dup = true;
                                lastestSelectedG = _game;
                            }
                        }

                        // 2cases: 1 add new/ edit existed game
                        if (!dup) {
                            Game game = new Game();
                            game.setGame_name(gameName);
                            games.getGames().add(game);
                        } else {
                            // add existed game to temp then romove then add back for that game get the lastest position
                            if (lastestSelectedG != null) {
                                Game tmp = lastestSelectedG;
                                games.getGames().remove(tmp);
                                games.getGames().add(lastestSelectedG);
                            }
                        }

                    } else {
                        Game game = new Game();
                        game.setGame_name(gameName);
                        games.getGames().add(game);
                    }
                    navigator("PickingActivity");
                    break;
            }
            return false;
        }
    };

    private void popReplayTicket() {

        //popup add money
        LayoutInflater layoutInflater = (LayoutInflater) SelectGameActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View pop_replay = layoutInflater.inflate(R.layout.pop_replay, null);


        ImageButton btnOk = pop_replay.findViewById(R.id.btnOk);
        ImageButton btnCancel = pop_replay.findViewById(R.id.btnCancel);
        final EditText txtTicketNo = pop_replay.findViewById(R.id.txtRepNo);

        //instantiate popup window
        final PopupWindow _popupWindow = new PopupWindow(pop_replay, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        _popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        //add blur for background
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        Ultis.applyDim(root, 0.8f); //or clearDim()

        //get parent size and set size to popup
        WindowManager wm = (WindowManager) SelectGameActivity.this.getSystemService(Context.WINDOW_SERVICE);
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
        _popupWindow.setWidth((int) measuredWidth);
        _popupWindow.setHeight((int) measuredHeight);


        //set focus for keyboard
        _popupWindow.setOutsideTouchable(true);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        txtTicketNo.requestFocus();

        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);// --> auto show softkey

        // OverWrite dismiss with include clear dim and softkey
        _popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Ultis.clearDim(root);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                _popupWindow.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _popupWindow.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                games.setPaypal("credit_more_money");
                _popupWindow.dismiss();

                new ReqFTPserver_ReqReplayTicket().execute(txtTicketNo.getText().toString());
                try{
                    popupWindow = Ultis.popLoading(SelectGameActivity.this, mainView);
                }catch (Exception e){
                    System.out.println(e);
                }


            }
        });
    }

    //TODO:get acc balance
    private class ReqFTPserver_AccBal extends AsyncTask<Void, Void, Boolean> {
        JsonObject accInfo = null;

        @Override
        protected Boolean doInBackground(Void... voids) {

            Context context = SelectGameActivity.this.getApplicationContext();
            accInfo = Ultis.getAccBalance(context, games.getPhone_no());
            if (accInfo != null)
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("getAccBalance", result.toString());
            try {
                if (result) {
                    popupWindow.dismiss();
                    addControl();
                    games.setCreditBal(accInfo.get("credit").toString());
                    games.setPointBal(accInfo.get("point").toString());
                } else {
                    popupWindow.dismiss();
                    Toast.makeText(getApplicationContext(), "please check your account balance", Toast.LENGTH_LONG).show();
                }

            } catch (JsonIOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    //TODO:request replay ticket
    private class ReqFTPserver_ReqReplayTicket extends AsyncTask<String, Void, Boolean> {
        String ticketNo = null;

        @Override
        protected Boolean doInBackground(final String... strings) {
            final Context context = SelectGameActivity.this.getApplicationContext();
            boolean req = Ultis.repReplayTicket(context, strings[0]);
            ticketNo = strings[0];
            if (req)
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("get replay ticket", result.toString());
//            popupWindow.dismiss();
            try {
                if (result) {
                    new ReqFTPserver_GetReplayTicket().execute(ticketNo);
                } else {
                    Toast.makeText(getApplicationContext(), "your ticket number is invalid", Toast.LENGTH_LONG).show();
                }

            } catch (JsonIOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    //TODO:get replay ticket
    private class ReqFTPserver_GetReplayTicket extends AsyncTask<String, Void, Boolean> {
        JsonObject rePlayTicket = null;

        @Override
        protected Boolean doInBackground(final String... strings) {

//            popupWindow = Ultis.popLoading(SelectGameActivity.this, mainView);
            final Context context = SelectGameActivity.this.getApplicationContext();
            rePlayTicket = Ultis.getReplayTicket(context, strings[0]);

            if (rePlayTicket != null)
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("get replay ticket", result.toString());
            popupWindow.dismiss();
            try {
                if (result) {
                    Games _tmpGame = new Gson().fromJson(rePlayTicket, Games.class);
                    if (games.getGames().size() > 0)
                        games.getGames().clear();
                    games.setGames(_tmpGame.getGames());
                    navigator("ViewGamesActivity");
                } else {
                    Toast.makeText(getApplicationContext(), "your ticket number is invalid", Toast.LENGTH_LONG).show();
                }

            } catch (JsonIOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private void navigator(String location) {
        Bundle bundle = new Bundle();
        Intent nextI = null;
        bundle.putString("from", SelectGameActivity.this.getClass().getSimpleName());
        bundle.putString("games", new Gson().toJson(games));
        switch (location) {
            case "PickingActivity":
                nextI = new Intent(SelectGameActivity.this, PickingActivity.class);
                break;
            case "ViewGamesActivity":
                nextI = new Intent(SelectGameActivity.this, ViewGamesActivity.class);
                break;
        }
        nextI.putExtras(bundle);
        startActivity(nextI);
    }
}
