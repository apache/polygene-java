/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.test.model;

import org.qi4j.api.annotation.Modifies;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
public class StandardThisModifier
    implements StandardThis
{
    // Attributes ----------------------------------------------------
    @Modifies StandardThis next;

    // Z implementation ----------------------------------------------
    public String bar()
    {
        return next.bar();
    }
}
