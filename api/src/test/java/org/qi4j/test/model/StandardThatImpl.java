/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.test.model;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
public class StandardThatImpl
    implements StandardThat
{
    // Z implementation ----------------------------------------------
    @FooAnnotation("Hello World")
    public String foo(String aString)
    {
        return "foo:"+aString;
    }

    public String test(String aString)
    {
        return "test:"+aString;
    }
}
