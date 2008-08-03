package org.qi4j.library.http;

import java.io.Serializable;
import java.util.Map;

public class FilterInfo implements Serializable
{
    public FilterInfo( String path, Map<String, String> initParameters, Dispatchers dispatchers )
    {
        this.dispatchers = dispatchers;
        this.initParameters = initParameters;
        this.path = path;
    }

    private static final long serialVersionUID = 1L;

    private final String path;
    private final Map<String, String> initParameters;
    private final Dispatchers dispatchers;
    
    public String getPath()
    {
        return path;
    }

    public Map<String, String> initParameters()
    {
        return initParameters;
    }

    public Dispatchers dispatchers()
    {
        return dispatchers;
    }
}
