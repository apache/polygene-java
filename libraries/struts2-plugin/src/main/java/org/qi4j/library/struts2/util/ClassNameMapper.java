/**
 *
 */
package org.qi4j.library.struts2.util;

import java.util.function.Function;

public interface ClassNameMapper extends Function<Class<?>,String>
{
    @Override
    String apply( Class<?> type );
}