/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.runtime;

import org.qi4j.api.annotation.ImplementedBy;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
@ImplementedBy(DomainInterfaceImpl.class)
public interface DomainInterface
{
    // Public --------------------------------------------------------
    String getFoo();

    void setFoo( String foo );
}
