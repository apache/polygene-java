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
public class CustomDomainInterfaceImpl
    implements DomainInterface
{
    String foo;

    // DomainInterface implementation --------------------------------
    public String getFoo()
    {
        return "FOO:"+foo;
    }

    public void setFoo( String foo )
    {
        this.foo = "foo:"+foo;
    }
}
