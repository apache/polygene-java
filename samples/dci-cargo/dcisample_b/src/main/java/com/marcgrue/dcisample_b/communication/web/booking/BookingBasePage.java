package com.marcgrue.dcisample_b.communication.web.booking;

import com.marcgrue.dcisample_b.communication.web.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Booking base page - to control the selected tab
 */
public class BookingBasePage extends BasePage
{
    public BookingBasePage()
    {
        super( "booking" );
    }

    public BookingBasePage( PageParameters pageParameters )
    {
        super( "booking", pageParameters );
    }
}