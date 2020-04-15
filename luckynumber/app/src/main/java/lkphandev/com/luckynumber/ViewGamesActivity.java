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

import java.util.ArrayList;
import java.util.HashMap;

//import android.os.Handler;
//import android.widget.ArrayAdapter;
//import android.widget.Button;

//import static java.lang.Integer.parseInt;

public class ViewGamesActivity extends AppCompatActivity {
    ListView listTicket;
    TextView lbGameType;
    TextView lbTotals;
    ImageButton btnBack;
    ImageButton btnSubmit;
    View popup_editGame;
    FlexLayout mainView;

    ArrayList<HashMap<String, String>> gameList = null;
    Games games;
    PopupWindow popupWindow = null;
    SimpleAdapter listTicketAdapter;

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
        setContentView(R.layout.activity_view_games);
        gameList = new ArrayList<HashMap<String, String>>();
        games = getGames();
        addControl();
    }

    private void addControl() {
        mainView = findViewById(R.id.mainView);
        lbGameType = findViewById(R.id.lb_game_type);
//        lbGameType.setText(game.getGame_name());
        lbTotals = findViewById(R.id.lbTotals);
//        games.setTotals(totalMoney());

        listTicket = findViewById(R.id.listGames);
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);


        //Custome listitem layout and expand adapter columns
        listTicketAdapter = new SimpleAdapter(
                ViewGamesActivity.this,
                gameList,
                R.layout.cus_list_item,
                new String[]{"game_name", "picked_no", "type", "money"},
                new int[]{R.id.lb_gameName, R.id.lb_picked_no, R.id.lb_game_type, R.id.lb_money}
        );
        populateListGames();
        lbTotals.setText(totalMoney());

        listTicket.setAdapter(listTicketAdapter);
        listTicket.setOnItemClickListener(selectedTicketListener);


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
                        bundle.putString("from", ViewGamesActivity.this.getClass().getSimpleName());
                        Intent prevI;

                        //just check condition for view local ticket
                        // if it has history ticket --> no need to pass games obj
                        String prevAct = getPrevAct();
                        bundle.putString("games", new Gson().toJson(games));

                        if( ViewLocalHistoryActivity.class.getSimpleName().contains(prevAct)){
                            prevI = new Intent(ViewGamesActivity.this, ViewLocalHistoryActivity.class);
                        }else{
                            prevI = new Intent(ViewGamesActivity.this, PickingActivity.class);
                        }

                        prevI.putExtras(bundle);
                        startActivity(prevI);

                        break;
                }
                return false;
            }
        });

        btnSubmit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnSubmit.setBackgroundResource(R.drawable.cus_btn_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnSubmit.setBackgroundResource(R.drawable.cus_btn_normal);
                        games.calculateMoney();

                        //TODO: validate amount balance and subgame in game
                        if (validateAmount() && games.getTotalTickets() > 0) {
                            Bundle bundle = new Bundle();
                            bundle.putString("from", ViewGamesActivity.this.getClass().getSimpleName());
                            bundle.putString("games", new Gson().toJson(games));
                            Intent nextI = new Intent(ViewGamesActivity.this, PaymentActivity.class);
                            nextI.putExtras(bundle);
                            startActivity(nextI);
                        }else
                            Toast.makeText(getApplicationContext(),"please check your tickets or account balance",Toast.LENGTH_SHORT).show();

                        break;
                }
                return false;
            }
        });


    }

    private AdapterView.OnItemClickListener selectedTicketListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {

            String ticketNo = gameList.get(position).get("picked_no");
            String gameName = sysGameName(gameList.get(position).get("game_name"));

            Game selectedGame = games.getGameByName(gameName);
            Ticket selectedTicket = selectedGame.getTicketByNo(ticketNo);

            popEditGame(mainView, selectedGame, selectedTicket, position);

        }
    };

    private void populateListGames() {
        for (Game _game : games.getGames()) {
            for (Ticket ticket : _game.getTickets()) {
                HashMap map = new HashMap();
                map.put("game_name", usrGameName(_game.getGame_name()));
                map.put("picked_no", ticket.getGame_no());
                map.put("type", ticket.getGame_type());
                map.put("money", ticket.getAmount());
                gameList.add(map);
            }
        }
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
        LayoutInflater layoutInflater = (LayoutInflater) ViewGamesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        WindowManager wm = (WindowManager) ViewGamesActivity.this.getSystemService(Context.WINDOW_SERVICE);
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

                //TODO: DELETE subgame in game for games list
                game.deleteTicket(ticket);
                popupWindow.dismiss();
                //use simple adapter --> just check list dataset update
                gameList.remove(selectedPos);
                lbTotals.setText(totalMoney());
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
                        gameList.get(selectedPos).put("money", ticket.getAmount());
                        lbTotals.setText(totalMoney());
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

    private String getPrevAct(){

        try{
            Bundle bundle = getIntent().getExtras();
            return bundle.getString("from");
        }catch (NullPointerException err){
            Log.d("getPrevAct",err.getMessage());
        }

        return null;
    }

    private String totalMoney() {
        int _money = 0;
        for (HashMap ticket : gameList) {
            _money += Double.parseDouble(ticket.get("money").toString());
        }
        games.calculateMoney();
        return String.valueOf(_money);
    }

    // validate edit amount with acct balance
    private Boolean validateAmount(String newBid, String current) {


        try{
            int _newBid = Integer.parseInt(newBid);
            int _current = Integer.parseInt(current);
            int amount = Integer.parseInt(games.getTotals());
            int credit = Integer.parseInt(games.getCreditBal());
            int point = Integer.parseInt(games.getPointBal());

            if ((amount - _current + _newBid) > credit && (amount - _current + _newBid) > point)
                return false;
            return true;

        }catch (Exception e){
            Log.e("validateAmount","ViewGameActivity(x,x) - " + e.getMessage());
        }

        return false;

    }

    //validata total with acct balance
    private Boolean validateAmount() {

        try{
            int amount = Integer.parseInt(games.getTotals());
            int credit = Integer.parseInt(games.getCreditBal());
            int point = Integer.parseInt(games.getPointBal());

            if (amount > credit && amount > point)
                return false;
            return true;

        }catch (Exception e){
            Log.e("validateAmount","ViewGameActivity() - " + e.getMessage());
        }

        return false;
    }
}
