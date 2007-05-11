/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package iop.api.persistence.modifier;

import iop.api.annotation.Modifies;
import iop.api.annotation.Uses;
import iop.api.persistence.Identity;

/**
 * This modifier ensures that the identity is correct.
 */
public final class IdentityValidatorModifier
    implements Identity
{
    @Uses Identity currentIdentity;
    @Modifies Identity identity;

    public void setIdentity( String anIdentity )
    {
        if( anIdentity == null )
        {
            throw new NullPointerException( "Identity may not be null" );
        }

        if( currentIdentity.getIdentity() != null )
        {
            throw new IllegalArgumentException( "Identity already set" );
        }

        identity.setIdentity( anIdentity );
    }

    public String getIdentity()
    {
        return identity.getIdentity();
    }
}
