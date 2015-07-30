/*
 * Copyright (c) 2009, Tonny Kohar. All Rights Reserved.
 * Copyright (c) 2015, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.apache.zest.tools.model.descriptor;

import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.functional.Visitable;
import org.apache.zest.functional.Visitor;

/**
 * Value Detail Descriptor.
 */
public class ValueDetailDescriptor
    extends CompositeDetailDescriptor<ValueDescriptor>
    implements Visitable<ValueDetailDescriptor>
{
    ValueDetailDescriptor( ValueDescriptor aDescriptor )
    {
        super( aDescriptor );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super ValueDetailDescriptor, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
    }
}
