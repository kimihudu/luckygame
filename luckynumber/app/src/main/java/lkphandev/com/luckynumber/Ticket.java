package lkphandev.com.luckynumber;

/**
 * Created by kimiboo on 2018-02-19.
 */

public class Ticket {
//    {
//                "pickedNo":"123456789",
//                    "type":"c1",
//                    "amount":100
//            }

    private String game_type;
    private String id;
    private String game_no;
    private String amount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGame_type() {
        return game_type;
    }

    public void setGame_type(String game_type) {
        this.game_type = game_type;
    }

    public String getGame_no() {
        return game_no;
    }

    public void setGame_no(String game_no) {
        this.game_no = game_no;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }


}
