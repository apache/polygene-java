package org.qi4j.runtime.property;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.runtime.composite.ConstraintsCheck;

import java.lang.reflect.Type;

/**
 * TODO
 */
public interface PropertyInfo
    extends ConstraintsCheck
{
    boolean isImmutable();
    QualifiedName qualifiedName();
    Type type();
}
