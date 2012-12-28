/*
 * Copyright (c) 2009, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.service.qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Specification;

/**
 * Filter services based on Meta Info being declared on the Service. Meta Info of any type can be set on the service
 * during assembly, e.g.;
 * <code><pre>
 * module.addService( MyService.class ).setMetaInfo( new MyCustomInfo(someData) );
 * </pre></code>
 * <p/>
 * and then at an injection point you can do this:
 * <code><pre>
 * &#64;Service &#64;HasMetaInfo(MyCustomInfo.class) MyService service;
 * </pre></code>
 * to get only a service that has a MyCustomInfo instance set as meta info.
 */
@Retention( RetentionPolicy.RUNTIME )
@Qualifier( HasMetaInfo.HasMetaInfoQualifier.class )
@Documented
public @interface HasMetaInfo
{
    /**
     * The Class(es) needed to have been defined in the Service meta info for a qualifier to evaluate true.
     *
     * @return One or more classes that should be defined in the service's meta info for the service to be considered
     *         qualified. If more than one class is defined, the {@code anded()} parameter will define if they must be
     *         AND'ed or OR'ed together.
     */
    Class[] value();

    /**
     * True if the Classes defined in the value() field should be AND'ed instead of OR'ed.
     *
     * @return If true, all the Class types defined in {@code value()} must be defined for the service for it to be
     *         qualified. If false, if any of the Class types defined in {@code value()} is defined for the service
     *         the service is qualified.
     */
    boolean anded() default false;

    /**
     * HasMetaInfo Annotation Qualifier.
     * See {@see HasMetaInfo}.
     */
    public static class HasMetaInfoQualifier
        implements AnnotationQualifier<HasMetaInfo>
    {
        @Override
        public <T> Specification<ServiceReference<?>> qualifier( final HasMetaInfo hasMetaInfo )
        {
            return new Specification<ServiceReference<?>>()
            {
                @Override
                public boolean satisfiedBy( ServiceReference<?> service )
                {
                    for( Class metaInfoType : hasMetaInfo.value() )
                    {
                        Object metaInfo = service.metaInfo( metaInfoType );
                        if( hasMetaInfo.anded() )
                        {
                            if( metaInfo == null )
                            {
                                return false;
                            }
                        }
                        else
                        {
                            if( metaInfo != null )
                            {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
        }
    }
}
