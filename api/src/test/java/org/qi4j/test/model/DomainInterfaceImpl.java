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
@ModifiedBy(DomainValidationModifier.class)
public class DomainInterfaceImpl
    implements DomainInterface
{
    // Attributes ----------------------------------------------------
    String foo;

    // DomainInterface implementation --------------------------------
    public String getFoo()
    {
        return foo;
    }

    public void setFoo( String foo )
    {
        this.foo = foo;
    }
}
