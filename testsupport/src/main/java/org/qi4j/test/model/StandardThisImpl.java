/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.test.model;

import org.qi4j.api.annotation.ModifiedBy;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
@ModifiedBy(StandardThisThatModifier.class)
public class StandardThisImpl
    implements StandardThis
{
    // Z implementation ----------------------------------------------
    public String bar()
    {
        return "bar";    
    }
}
