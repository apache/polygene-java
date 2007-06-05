/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.runtime;

import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
public class StandardThisThatModifier
    implements StandardThat
{
    // Attributes ----------------------------------------------------
    @Uses StandardThis meAsThis;
    @Modifies StandardThat next;

    // Z implementation ----------------------------------------------
    public void foo( String aString )
    {
        meAsThis.bar();

        next.foo( aString);
    }

    public void test( String aString )
    {
        next.test( aString);
    }
}
