/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.runtime;

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
    void foo(String aString);
    void test(String aString);
}
