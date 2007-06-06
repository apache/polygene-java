/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.test.model;

import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
@ImplementedBy(StandardThatImpl.class)
@ModifiedBy(StandardThatModifier.class)
public interface StandardThat
{
    // Public --------------------------------------------------------
    String foo(String aString);
    String test(String aString);
}
