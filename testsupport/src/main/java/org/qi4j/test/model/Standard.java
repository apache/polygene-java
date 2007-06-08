/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.test.model;

import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.ObjectStrategy;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
@ImplementedBy(StandardThisImpl.class)
@ModifiedBy({StandardThisModifier.class, FooModifier.class})
public interface Standard
    extends StandardThis, StandardThat, ObjectStrategy
{
}
