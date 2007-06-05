/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.runtime;

import org.qi4j.api.annotation.Modifies;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
public class StandardThatModifier
    implements StandardThat
{
    // Attributes ----------------------------------------------------
    @Modifies StandardThat next;

    // Z implementation ----------------------------------------------
    public void foo( String aString )
    {
        next.foo( aString);
    }

    public void test( String aString )
    {
        next.test( aString);
    }
}
