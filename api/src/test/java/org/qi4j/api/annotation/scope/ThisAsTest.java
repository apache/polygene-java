/*
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.api.annotation.scope;

import java.lang.annotation.Annotation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * Tests public api exposed by ConcernFor annotation.
 * This will ensure that the public api does not get changed by mistake.
 */
public class ThisAsTest
{

    @Test
    public void retention() throws NoSuchFieldException
    {
        Annotation[] annotations = Annotated.class.getDeclaredField( "uses" ).getDeclaredAnnotations();
        assertNotNull( "annotations should not be null", annotations );
        assertEquals( "number of annotations", 1, annotations.length );
        assertEquals( "annotation type", ThisAs.class, annotations[ 0 ].annotationType() );
    }

    @Test
    public void defaultOptionalIsFalse() throws NoSuchFieldException
    {
        Annotation[] annotations = Annotated.class.getDeclaredField( "uses" ).getDeclaredAnnotations();
        if( annotations != null && annotations.length > 0 && ThisAs.class.equals( annotations[ 0 ].annotationType() ) )
        {
            assertEquals( "default optional value", false, ( (ThisAs) annotations[ 0 ] ).optional() );
        }
    }

    private static class Annotated
    {
        @ThisAs String uses;
        @ThisAs( optional = true ) String usesOptional;
    }

}
