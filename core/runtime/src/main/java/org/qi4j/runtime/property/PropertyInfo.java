package org.qi4j.runtime.property;

import java.lang.reflect.Type;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.runtime.composite.ConstraintsCheck;

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
