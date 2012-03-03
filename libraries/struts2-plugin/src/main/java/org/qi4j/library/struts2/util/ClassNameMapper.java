/**
 *
 */
package org.qi4j.library.struts2.util;

import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;

public interface ClassNameMapper extends Function<Class<?>,String>
{
    @Override
    String map( Class<?> type );
}