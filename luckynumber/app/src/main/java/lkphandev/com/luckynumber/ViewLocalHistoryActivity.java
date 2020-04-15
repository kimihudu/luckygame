package lkphandev.com.luckynumber;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mmin18.widget.FlexLayout;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

//import android.support.v7.app.AlertDialog;
//import java.util.Collections;
//import java.util.Comparator;

//import static lkphandev.com.luckynumber.R.id.listTickets;


public class ViewLocalHistoryActivity extends AppCompatActivity {
    //        games.setTotals(totalMoney());

    ListView listTicket;
    TextView lbGameType;
    TextView lbSelected;
    ImageButton btnBack;
    ImageButton btnView;
    View popup_editGame;
    FlexLayout mainView;
    int selectedPos = 0;

    ArrayList<HashMap<String, String>> ticketList = null;
    Games games;
    Games historyGames;
    PopupWindow popupWindow = null;
    SimpleAdapter listTicketAdapter;
    ArrayList<File> localHistory = null;

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
        setContentView(R.layout.activity_view_games_local);

        //012420lk - keep the current credit from launch in history view
        games = getGames();

        //011120lk - update for view local history tickets
        //get all list file in local
        //pass to listview data
        //get list file local

        ticketList = new ArrayList<HashMap<String, String>>();
        localHistory =  Ultis.getLocalTicket();

        if(!localHistory.isEmpty()){
            //pass the list to listview data
            ticketList = getTicketHistory(localHistory);
            addControl();
            commonControl();

        }else{

            //011120lk - update for view local history tickets
            //if dont have file history
            //show msg and return to previous activity
            Toast.makeText(getApplicationContext(), "Sorry! we couldn't load or find your local history ticket", Toast.LENGTH_LONG).show();
            commonControl();

        }

    }

    private void commonControl(){

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

                        //list game should be empty when go to Select games
                        games.setGames(new ArrayList<Game>());
                        Bundle bundle = new Bundle();
                        bundle.putString("from", ViewLocalHistoryActivity.this.getClass().getSimpleName());
                        bundle.putString("games", new Gson().toJson(games));
                        Intent prevI = new Intent(ViewLocalHistoryActivity.this, SelectGameActivity.class);
                        prevI.putExtras(bundle);
                        startActivity(prevI);
                        break;
                }
                return false;
            }
        });
    }
    private void addControl() {
        mainView = findViewById(R.id.mainView);
        lbGameType = findViewById(R.id.lb_game_type);
        btnView = findViewById(R.id.btnView);
        listTicket = findViewById(R.id.listTickets);


        //Custome list ticket layout and expand adapter columns

        listTicketAdapter = new SimpleAdapter(
                ViewLocalHistoryActivity.this,
                ticketList,
                R.layout.cus_list_ticket,
                new String[]{"date", "ticket_no", "money"},
                new int[]{R.id.lb_date, R.id.lb_ticket_no, R.id.lb_money}
        );


//        lbTotals.setText(totalMoney());

        listTicket.setAdapter(listTicketAdapter);
        listTicket.setOnItemClickListener(selectedTicketListener);




        btnView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnView.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnView.setBackgroundResource(R.drawable.cus_btn_normal);

                        //just get list games not get credit history
                        games.setGames(historyGames.getGames());
                        Bundle bundle = new Bundle();
                        bundle.putString("from", ViewLocalHistoryActivity.this.getClass().getSimpleName());
                        bundle.putString("games", new Gson().toJson(games));
                        Intent nextI = new Intent(ViewLocalHistoryActivity.this, ViewGamesActivity.class);
                        nextI.putExtras(bundle);
                        startActivity(nextI);


                        break;
                }
                return false;
            }
        });


    }

    //TODO: get ticket detail from click  and pass to ticketdetail activity
    private AdapterView.OnItemClickListener selectedTicketListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {

            //011120lk - update for view local history tickets
            //read the ticket file
            //return to a Games

            //remove * from prev selected item
            View resetSelectedItem = listTicket.getChildAt(selectedPos);
            lbSelected = resetSelectedItem.findViewById(R.id.lb_ticket_selected);
            lbSelected.setText("");

            //show selected item
            lbSelected = v.findViewById(R.id.lb_ticket_selected);
            lbSelected.setText("*");
            selectedPos = position;

            String ticketNo = ticketList.get(position).get("ticket_no");

            for(File _file : localHistory) {

                if(_file.getName().contains(ticketNo)){

                    //TODO: CONVERT LOCAL JSON FILE TO GAMES OBJ
                    historyGames = (Games) Ultis.jsonFile2Obj(_file);
                     Log.d("games ticket","obj games " + historyGames.toString());
                }
            }

        }
    };

    //011120lk - update for view local history tickets
    //TODO: get all local ticket file
    private ArrayList getTicketHistory(ArrayList<File> tickets) {

        ArrayList _ticketList = new ArrayList();

        for (File _file : tickets) {

            String ticketNo = Ultis.getTicketLocal((_file.getName()))[1];
            String ticketTotal = Ultis.getTicketLocal((_file.getName()))[0];
            String ticketDate = Ultis.convertTime(_file.lastModified());


            HashMap map = new HashMap();
            map.put("date", ticketDate);
            map.put("ticket_no", ticketNo);
            map.put("money", ticketTotal);
            _ticketList.add(map);
        }


        return _ticketList;
    }

    private String usrGameName(String gameName) {
        String result = "";
        switch (gameName) {
            case "G1":
                result = "M";
                break;
            case "G2":
                result = "S";
                break;
            case "G3":
                result = "D";
                break;
            case "G4":
                result = "G";
                break;
            case "G5":
                result = "P4";
                break;
            case "G6":
                result = "123";
                break;
        }
        return result;
    }

    private String sysGameName(String gameName) {
        String result = "";
        switch (gameName) {
            case "M":
                result = "G1";
                break;
            case "S":
                result = "G2";
                break;
            case "D":
                result = "G3";
                break;
            case "G":
                result = "G4";
                break;
            case "P4":
                result = "G5";
                break;
            case "123":
                result = "G6";
                break;
        }
        return result;
    }

    private void popEditGame(View v, final Game game, final Ticket ticket, final int selectedPos) {

        final int MAX_NO = 5;
        //popup add money
        LayoutInflater layoutInflater = (LayoutInflater) ViewLocalHistoryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popup_editGame = layoutInflater.inflate(R.layout.pop_edit_game, null);

        ImageButton btnSave = popup_editGame.findViewById(R.id.btnSave);
        ImageButton btnDelete = popup_editGame.findViewById(R.id.btnDelete);
        TextView lbGameNo = popup_editGame.findViewById(R.id.lb_gameNo);
        ImageButton btnCancel = popup_editGame.findViewById(R.id.btnCancel);
        final EditText txtAmountMoney = popup_editGame.findViewById(R.id.txtAmount);
        txtAmountMoney.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NO)});
        txtAmountMoney.setText(ticket.getAmount());

        //instantiate popup window
        popupWindow = new PopupWindow(popup_editGame, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);


        //add blur for background
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        Ultis.applyDim(root, 0.8f); //or clearDim()

        //get parent size and set size to popup
        WindowManager wm = (WindowManager) ViewLocalHistoryActivity.this.getSystemService(Context.WINDOW_SERVICE);
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
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        txtAmountMoney.requestFocus();
        popupWindow.update();
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        txtAmountMoney.requestFocus();
        //auto popup softkey
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


        // OverWrite dismiss with include clear dim and softkey
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Ultis.clearDim(root);
                popupWindow.dismiss();
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                game.deleteTicket(ticket);
                popupWindow.dismiss();
                //use simple adapter --> just check list dataset update
                ticketList.remove(selectedPos);
//                lbTotals.setText(totalMoney());
                listTicketAdapter.notifyDataSetChanged();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (txtAmountMoney.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "please enter your amount", Toast.LENGTH_SHORT).show();
                } else {
                    if (!validateAmount(txtAmountMoney.getText().toString(), ticket.getAmount())) {
                        Toast.makeText(getApplicationContext(), "please check your Account balance", Toast.LENGTH_SHORT).show();
                    } else {
                        ticket.setAmount(txtAmountMoney.getText().toString());
                        game.editTicket(ticket); //--> edit the old ticket
                        popupWindow.dismiss();
                        //use simple adapter --> just check list dataset update
                        //update hasmap content in arraylist
                        ticketList.get(selectedPos).put("money", ticket.getAmount());
//                        lbTotals.setText(totalMoney());
                        games.calculateMoney();
                        listTicketAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private Games getGames() {
        Games returnGames = null;
        String gamesObj;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            gamesObj = bundle.getString("games");
            returnGames = new Gson().fromJson(gamesObj, Games.class);
        }
        return returnGames;
    }

    private String totalMoney() {
        int _money = 0;
        for (HashMap ticket : ticketList) {
            _money += Double.parseDouble(ticket.get("money").toString());
        }
        games.calculateMoney();
        return String.valueOf(_money);
    }

    // validate edit amount with acct balance
    private Boolean validateAmount(String newBid, String current) {
        int _newBid = Integer.parseInt(newBid);
        int _current = Integer.parseInt(current);
        int amount = Integer.parseInt(games.getTotals());
        int credit = Integer.parseInt(games.getCreditBal());
        int point = Integer.parseInt(games.getPointBal());

        if ((amount - _current + _newBid) > credit && (amount - _current + _newBid) > point)
            return false;
        return true;
    }

    //validata total with acct balance
    private Boolean validateAmount() {
        int amount = Integer.parseInt(games.getTotals());
        int credit = Integer.parseInt(games.getCreditBal());
        int point = Integer.parseInt(games.getPointBal());

        if (amount > credit && amount > point)
            return false;
        return true;
    }
}
