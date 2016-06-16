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
 *
 *
 */

package org.apache.zest.bootstrap;

import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.common.InvalidApplicationException;
import org.apache.zest.api.mixin.NoopMixin;
import org.apache.zest.api.property.IllegalTypeException;
import org.junit.Test;

public class InvalidTypesTest
{
    public void givenMethodWithStringWhenDeclaringExpectOk()
        throws Throwable
    {
        bootWith( Interface0.class );
    }

    @Test( expected = IllegalTypeException.class )
    public void givenMethodWithJavaUtilDateWhenDeclaringExpectException()
        throws Throwable
    {
        bootWith( Interface1.class );
    }

    @Test( expected = IllegalTypeException.class )
    public void givenMethodWithJavaUtilCalendarWhenDeclaringExpectException()
        throws Throwable
    {
        bootWith( Interface2.class );
    }

    @Test( expected = IllegalTypeException.class )
    public void givenMethodWithJavaSqlTimeWhenDeclaringExpectException()
        throws Throwable
    {
        bootWith( Interface3.class );
    }

    @Test( expected = IllegalTypeException.class )
    public void givenMethodWithJavaSqlDateWhenDeclaringExpectException()
        throws Throwable
    {
        bootWith( Interface4.class );
    }

    @Test( expected = IllegalTypeException.class )
    public void givenMethodWithJavaTextDateFormatWhenDeclaringExpectException()
        throws Throwable
    {
        bootWith( Interface5.class );
    }

    @Test( expected = IllegalTypeException.class )
    public void givenMethodWithJavaTextSimpleDateFormatWhenDeclaringExpectException()
        throws Throwable
    {
        bootWith( Interface6.class );
    }

    private void bootWith( Class<?> type )
        throws Throwable
    {
        try
        {
            new SingletonAssembler()
            {

                @Override
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.values( type ).withMixins( NoopMixin.class );
                }
            };
        }
        catch( AssemblyException | ActivationException e )
        {
            Throwable cause = e.getCause();
            if( cause instanceof InvalidApplicationException )
            {
                throw cause.getCause();
            }

        }
    }

    interface Interface0
    {
        void doSomething( String abc );
    }

    interface Interface1
    {
        void doSomething( java.util.Date arg1 );
    }

    interface Interface2
    {
        void doSomething( String abc, java.util.Calendar arg1 );
    }

    interface Interface3
    {
        void doSomething( String abc, java.sql.Time arg1 );
    }

    interface Interface4
    {
        void doSomething( String abc, java.sql.Date arg1 );
    }

    interface Interface5
    {
        void doSomething( String abc, java.text.DateFormat arg1 );
    }

    interface Interface6
    {
        void doSomething( String abc, java.text.SimpleDateFormat arg1 );
    }

}
