/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
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
package org.qi4j.library.general.model;

import org.qi4j.Composite;
import org.qi4j.annotation.Concerns;
import org.qi4j.library.general.test.model.DescriptorConcern;

public class DescriptorTest extends AbstractTest
{
    public void testDescriptorAsMixin() throws Exception
    {
        DummyComposite composite = builderFactory.newCompositeBuilder( DummyComposite.class ).newInstance();
        composite.setDisplayValue( "Sianny" );
        String displayValue = composite.getDisplayValue();
        assertEquals( displayValue, composite.getDisplayValue() );
    }

    public void testDescriptorWithModifier() throws Exception
    {
        DummyComposite2 composite = builderFactory.newCompositeBuilder( DummyComposite2.class ).newInstance();
        composite.setDisplayValue( "Sianny" );
        String displayValue = composite.getDisplayValue();
        assertEquals( displayValue, "My name is Sianny" );
    }

    private interface DummyComposite extends Descriptor, HasName, Composite
    {
    }

    @Concerns( { DescriptorConcern.class } )
    private interface DummyComposite2 extends Descriptor, HasName, Composite
    {
    }
}
