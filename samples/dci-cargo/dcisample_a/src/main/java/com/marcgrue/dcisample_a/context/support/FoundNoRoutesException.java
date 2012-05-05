package com.marcgrue.dcisample_a.context.support;

import org.joda.time.LocalDate;

/**
 * Custom messages when the deadline is too close and we can't find a route.
 */
public class FoundNoRoutesException extends Exception
{
    private final String city;
    private final LocalDate deadline;

    public FoundNoRoutesException( String city, LocalDate deadline )
    {
        this.city = city;
        this.deadline = deadline;
    }

    @Override
    public String getMessage()
    {
        if (deadline.isBefore( new LocalDate().plusDays( 2 ) ))
        {
            return "Impossible to get the cargo to " + city + " before " + deadline
                  + "! Make a new booking with a deadline 2-3 weeks ahead in time.";
        }
        else if (deadline.isBefore( new LocalDate().plusDays( 4 ) ))
        {
            return "Couldn't find any routes arriving in " + city + " before " + deadline
                  + ". Please try again or make a new booking with a deadline 2-3 weeks ahead in time.";
        }
        else if (deadline.isBefore( new LocalDate().plusDays( 6 ) ))
        {
            return "Sorry, our system couldn't immediately find a route arriving in " + city + " before " + deadline
                  + ". Please try again, and we should hopefully be able to find a new route for you.";
        }

        return "Couldn't find any route to " + city + " arriving before " + deadline
              + ". We don't know why. Have a nice day.";
    }
}