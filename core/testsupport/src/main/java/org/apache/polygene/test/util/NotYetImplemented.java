/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.test.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//import junit.framework.AssertionFailedError;
//import org.junit.rules.TestRule;
//import org.junit.runner.Description;
//import org.junit.runners.model.Statement;

/**
 * JUnit annotation and rule to mark not yet implemented tests.
 *
 * In order to use this annotation you must register the corresponding {@link NotYetImplemented.Rule}.
 *
 * Not yet implemented tests are run an expected to fail, an assertion error is thrown if they pass.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Documented
public @interface NotYetImplemented
{
    String reason() default "";

//    class Rule implements TestRule
//    {
//        @Override
//        public Statement apply( Statement base, Description description )
//        {
//            if( description.getAnnotation( NotYetImplemented.class ) == null )
//            {
//                return base;
//            }
//            return new Statement()
//            {
//                @Override
//                public void evaluate() throws Throwable
//                {
//                    boolean passed = false;
//                    try
//                    {
//                        base.evaluate();
//                        passed = true;
//                    }
//                    catch( Throwable ex )
//                    {
//                        System.err.println( "Not yet implemented test expectedly failed" );
//                        ex.printStackTrace( System.err );
//                    }
//                    if( passed )
//                    {
//                        throw new AssertionFailedError(
//                            "Test " + description.getDisplayName()
//                            + " is annotated as not yet implemented, expected it to fail but it passed."
//                        );
//                    }
//                }
//            };
//        }
//    }
}
