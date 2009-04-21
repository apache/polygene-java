/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.serialization;

import org.junit.Test;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.StateName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
public class SerializableStateTest
{
    @Test
    public void testSerializeState() throws IOException, ClassNotFoundException
    {
        EntityReference ref = EntityReference.parseEntityReference( "1234" );
        String version = "abcd";
        long lastModified = 1234;
        Set<EntityTypeReference> entityTypeReferences = new HashSet<EntityTypeReference>();
        entityTypeReferences.add( new EntityTypeReference( TypeName.nameOf( getClass() ), null, "abcde" ) );

        Map<StateName, String> properties = new HashMap<StateName, String>();
        properties.put( new StateName( QualifiedName.fromName( "foo", "bar" ), null, "abcdef" ), "test" );

        Map<StateName, EntityReference> associations = new HashMap<StateName, EntityReference>();
        Map<StateName, List<EntityReference>> manyAssociations = new HashMap<StateName, List<EntityReference>>();
        SerializableState state = new SerializableState( ref, version, 1234, entityTypeReferences,
                                                         properties,
                                                         associations,
                                                         manyAssociations );

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( bout );
        out.writeObject( state );
        out.close();

        ByteArrayInputStream bin = new ByteArrayInputStream( bout.toByteArray() );
        ObjectInputStream oin = new ObjectInputStream( bin );
        SerializableState state2 = (SerializableState) oin.readObject();
        oin.close();

        System.out.println( bout.toByteArray().length );

    }
}
