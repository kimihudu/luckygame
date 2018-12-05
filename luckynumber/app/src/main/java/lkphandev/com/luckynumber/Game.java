package lkphandev.com.luckynumber;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by kimiboo on 2018-02-17.
 */

public class Game {

//    {
//            "game":"G1",
//                "ticket": [
//            {
//                "pickedNo":"123456789",
//                    "type":"c1",
//                    "amount":100
//            }
//      ]
//        }

    private String game_name;
    private ArrayList<Ticket> tickets = new ArrayList<>();
    private String total;


    public String getGame_name() {
        return game_name;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public ArrayList<Ticket> getTickets() {
        return tickets;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setTickets(ArrayList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void deleteTicket(@NonNull Ticket ticket) {
        ArrayList<Ticket> _tmpTickets = new ArrayList<>();
        for (Ticket _t : tickets) {
            if (!_t.getId().equals(ticket.getId())) {
                _tmpTickets.add(_t);
            }
        }
        setTickets(_tmpTickets);
        calculateMoney();
    }

    public void editTicket(Ticket ticket) {
        for (Ticket _t : tickets) {
            if (_t.getGame_no().equals(ticket.getGame_no()))
                _t.setAmount(ticket.getAmount());
        }
    }

    public Ticket getTicketByNo(String ticketNo) {
        for (Ticket ticket : tickets) {
            if (ticket.getGame_no().equals(ticketNo))
                return ticket;
        }
        return null;
    }

    public void calculateMoney() {
        int total = 0;
        for (Ticket ticket : tickets) {
            total += Double.parseDouble(ticket.getAmount());
        }
        setTotal(String.valueOf(total));
    }
}
