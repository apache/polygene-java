/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.entity;

/**
 * If you try to create an EntityComposite whose identity already exists,
 * then this exception will be thrown.
 */
public class EntityCompositeAlreadyExistsException
    extends EntitySessionException
{
    private String identity;
    private Class compositeType;

    public EntityCompositeAlreadyExistsException( String identity, Class compositeType )
    {
        super( "EntityComposite (" + identity + " of type " + compositeType.getName() + ") already exists." );
        this.identity = identity;
        this.compositeType = compositeType;
    }

    public String getIdentity()
    {
        return identity;
    }

    public Class getCompositeType()
    {
        return compositeType;
    }
}
