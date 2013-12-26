package org.qi4j.library.appbrowser;

import org.json.JSONException;

public interface Formatter<NODE, LEAF>
{

    void enter( NODE visited )
        throws JSONException;

    void leave( NODE visited )
        throws JSONException;

    void visit( LEAF visited )
        throws JSONException;
}
