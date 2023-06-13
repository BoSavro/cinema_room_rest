package cinema.controllers;

import java.util.*;
import cinema.dto.*;
import cinema.entities.Room;
import cinema.entities.Seat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IndexController {
    Room room;

    public IndexController() {
        this.room = new Room();
        room.setTotalRows(9);
        room.setTotalColumns(9);

        List<Seat> availableSeats = new ArrayList<>();
        for (int i = 1; i <= room.getTotalRows(); i++) {
            for (int j = 1; j <= room.getTotalColumns(); j++) {
                int price = 8;

                if (i <= 4) {
                    price = 10;
                }

                Seat seat = new Seat();
                seat.setRow(i);
                seat.setColumn(j);
                seat.setPrice(price);
                availableSeats.add(seat);
            }
        }

        room.setAvailableSeats(availableSeats);
    }

    @GetMapping("/seats")
    public String getSeats() throws JsonProcessingException {

        List<SeatDto> availableSeats = this.room.getAvailableSeats().stream()
                .map(seat -> new SeatDto(seat.getRow(),seat.getColumn(),seat.getPrice()))
                .toList();

        RoomDto roomDto = new RoomDto(
                this.room.getTotalRows(),
                this.room.getTotalColumns(),
                availableSeats
        );

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(roomDto);
    }

    @PostMapping(value = "/purchase", produces="application/json")
    public ResponseEntity<Map<String, ?>> purchase(@RequestBody PurchaseRequestDto purchaseRequestDto) throws JsonProcessingException {
        Seat seat = findSeatByParams(purchaseRequestDto.row(), purchaseRequestDto.column());

        if (seat == null) {
            Map<String, String> errorMsg = new HashMap<>();
            errorMsg.put("error", "The number of a row or a column is out of bounds!");
            return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
        }

        if (seat.isPurchase()) {
            Map<String, String> errorMsg = new HashMap<>();
            errorMsg.put("error", "The ticket has been already purchased!");
            return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
        }

        UUID token = UUID.randomUUID();
        seat.setToken(token);
        seat.setPurchase(true);

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("row", seat.getRow());
        ticket.put("column", seat.getColumn());
        ticket.put("price", seat.getPrice());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("ticket", ticket);

        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping(value = "/return", produces="application/json")
    public ResponseEntity<Map<String, ?>> returnTicket(@RequestBody ReturnRequestDto returnRequestDto) {
        Map<String, Object> responseData = new HashMap<>();
        System.out.println("[ " + returnRequestDto.token() + " ]");

        try {
            Seat seat = this.findSeatByToken(returnRequestDto.token());

            if (seat == null) {
                Map<String, String> errorMsg = new HashMap<>();
                errorMsg.put("error", "Wrong token!");
                return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
            }

            seat.setPurchase(false);

            Map<String, Object> ticket = new HashMap<>();
            ticket.put("row", seat.getRow());
            ticket.put("column", seat.getColumn());
            ticket.put("price", seat.getPrice());
            responseData.put("returned_ticket", ticket);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping(value = "/stats", produces="application/json")
    public ResponseEntity<Map<String, ?>> getStats(@RequestParam("password") Optional<String> password) {
        if (password.isEmpty()) {
            Map<String, String> errorMsg = new HashMap<>();
            errorMsg.put("error", "The password is wrong!");
            return new ResponseEntity<>(errorMsg, HttpStatus.UNAUTHORIZED);
        }

        List<Seat> seats = this.room.getAvailableSeats();

        int currentIncome = this.calculateIncome();
        long numberOfAvailableSeats = seats.stream()
                .filter(seat -> !seat.isPurchase())
                .count();
        long numberOfPurchasedTickets = seats.stream().filter(Seat::isPurchase).count();

        System.out.println("INCOME: " + currentIncome);


        Map<String, Object> result = new HashMap<>();
        result.put("current_income", currentIncome);
        result.put("number_of_available_seats", numberOfAvailableSeats);
        result.put("number_of_purchased_tickets", numberOfPurchasedTickets);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    private Seat findSeatByParams(int row, int column) {
        for (Seat s : this.room.getAvailableSeats()) {
            if (s.getRow() == row && s.getColumn() == column) {
                return s;
            }
        }

        return null;
    }

    private Seat findSeatByToken(UUID token) {
        for (Seat seat : this.room.getAvailableSeats()) {
            if (null != seat.getToken() && seat.getToken().equals(token)) {
                return seat;
            }
        }

        return null;
    }

    private int calculateIncome() {
        List<Seat> seats = this.room.getAvailableSeats();

        return seats.stream()
                .filter(Seat::isPurchase)
                .mapToInt(Seat::getPrice)
                .sum();
    }
}
