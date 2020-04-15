package lkphandev.com.luckynumber;

//import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by kimiboo on 2018-02-17.
 */

public class Games {
//    {
//        "phone":"1234567890",
//            "games": [
//        {
//            "game":"G1",
//                "detail": [
//            {
//                "pickedNo":"123456789",
//                    "type":"c1",
//                    "amount":100
//            }
//      ]
//        }
//  ]
//    }

    private ArrayList<Game> games = new ArrayList<>();
    private String phone_no;
    private String paypal;
    private String totals = "";
    private String location;
    private String creditBal = "";
    private String pointBal = "";

    public String getCreditBal() {
        return creditBal;
    }

    public void setCreditBal(String creditBal) {
        this.creditBal = creditBal;
    }

    public String getPointBal() {
        return pointBal;
    }

    public void setPointBal(String pointBal) {
        this.pointBal = pointBal;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTotals() {
        return totals;
    }

    public void setTotals(String totals) {
        this.totals = totals;
    }

    public String getPaypal() {
        return paypal;
    }

    public void setPaypal(String paypal) {
        this.paypal = paypal;
    }

    public ArrayList<Game> getGames() {
        return games;
    }

    public void setGames(ArrayList<Game> games) {
        this.games = games;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }


    public Game getGameByName(String gameName) {
        for (Game _game: games){
            if (_game.getGame_name().equals(gameName))
                return _game;
        }
        return null;
    }


    public void calculateMoney(){
        int totals = 0;
        for (Game game:games){
            game.calculateMoney();
            totals += Double.parseDouble(game.getTotal());
        }
        setTotals(String.valueOf(totals));
    }

    public int getTotalTickets() {
        int _totalTickets = 0;

        for (Game game:games) _totalTickets += game.getTickets().size();

        return _totalTickets;
    }


}
