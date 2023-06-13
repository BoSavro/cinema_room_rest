package cinema.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RoomDto(
        @JsonProperty("total_rows")
        int totalRows,

        @JsonProperty("total_columns")
        int totalColumns,

        @JsonProperty("available_seats")
        List<SeatDto> availableSeats
) {}
