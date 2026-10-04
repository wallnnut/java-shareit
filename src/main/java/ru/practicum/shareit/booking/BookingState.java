package ru.practicum.shareit.booking;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Unknown state: " + value);
        }
        for (BookingState state : values()) {
            if (state.name().equals(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown state: " + value);
    }
}
