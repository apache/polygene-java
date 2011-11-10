package org.qi4j.api.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Stack;

/**
 * TODO
 */
public class JSONObjectSerializer
    extends JSONSerializer
{
    Stack<Object> stack = new Stack<Object>();
    Stack<String> keys = new Stack<String>();

    private Object root;

    @Override
    public JSONSerializer key( String key ) throws JSONException
    {
        keys.push( key );
        return this;
    }

    @Override
    public JSONSerializer value( Object value ) throws JSONException
    {
        if (stack.empty())
            root = value;
        else if (stack.peek() instanceof JSONArray)
            ((JSONArray)stack.peek()).put( value );
        else
            ((JSONObject)stack.peek()).put( keys.pop(), value );
        return this;
    }

    @Override
    public JSONSerializer objectStart() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        if (stack.isEmpty())
            root = jsonObject;
        stack.push( jsonObject );
        return this;
    }

    @Override
    public JSONSerializer objectEnd() throws JSONException
    {
        value(stack.pop());
        return this;
    }

    @Override
    public JSONSerializer arrayStart() throws JSONException
    {
        JSONArray jsonArray = new JSONArray( );
        if (stack.isEmpty())
            root = jsonArray;
        stack.push( jsonArray );
        return this;
    }

    @Override
    public JSONSerializer arrayEnd() throws JSONException
    {
        value(stack.pop());
        return this;
    }

    public Object getRoot()
    {
        return root;
    }
}
