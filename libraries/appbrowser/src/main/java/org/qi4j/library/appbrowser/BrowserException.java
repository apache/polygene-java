package org.qi4j.library.appbrowser;

import org.json.JSONException;

public class BrowserException extends RuntimeException
{
    public BrowserException( String message, JSONException exception )
    {
        super( message, exception);
    }
}
