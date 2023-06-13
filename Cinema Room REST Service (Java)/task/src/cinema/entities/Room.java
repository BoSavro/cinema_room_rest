package cinema.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Room {
    @JsonProperty("total_rows")
    private int totalRows;

    @JsonProperty("total_columns")
    private int totalColumns;

    @JsonProperty("available_seats")
    private List<Seat> availableSeats;

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTotalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(int totalColumns) {
        this.totalColumns = totalColumns;
    }

    public List<Seat> getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(List<Seat> availableSeats) {
        this.availableSeats = availableSeats;
    }
}
