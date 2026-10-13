package com.aerofleet.exceptions;

/**
 * Custom Exception thrown when no eligible drone can be found for a mission.
 * Unit III: User-defined Exception Handling.
 */
public class NoRouteFoundException extends Exception {
    public NoRouteFoundException(String message) {
        super(message);
    }
}
