package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.library.appbrowser.Formatter;

public class NullFormatter
    implements Formatter<Object, Object>
{
    @Override
    public void enter( Object visited )
        throws JSONException
    {

    }

    @Override
    public void leave( Object visited )
        throws JSONException
    {

    }

    @Override
    public void visit( Object visited )
        throws JSONException
    {

    }
}
