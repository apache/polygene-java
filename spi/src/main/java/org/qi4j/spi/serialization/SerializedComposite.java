/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

import java.io.IOException;
import java.io.Serializable;
import org.qi4j.api.composite.Composite;

/**
 * TODO
 */
final class SerializedComposite
    implements Serializable
{
    private Object[] mixins;
    private Class<? extends Composite> type;

    public SerializedComposite( Object[] mixins, Class<? extends Composite> type )
    {
        this.type = type;
        this.mixins = mixins;
    }

    public Object[] mixins()
    {
        return mixins;
    }

    public Class<? extends Composite> type()
    {
        return type;
    }

    private void writeObject( java.io.ObjectOutputStream out )
        throws IOException
    {
        out.writeObject( mixins );

        String serializedClassNames = addClassNames( type );
        out.writeUTF( serializedClassNames );
    }

    private String addClassNames( Class compositeInterface )
    {
        String className = compositeInterface.getName();

        Class[] extendedInterfaces = compositeInterface.getInterfaces();
        for( Class extendedInterface : extendedInterfaces )
        {
            if( Composite.class.isAssignableFrom( extendedInterface ) && !Composite.class.equals( extendedInterface ) )
            {
                className += ":" + addClassNames( extendedInterface );
            }
        }

        return className;
    }

    private void readObject( java.io.ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        mixins = (Object[]) in.readObject();

        String serializedClassNames = in.readUTF();
        String[] classNames = serializedClassNames.split( ":" );
        for( String className : classNames )
        {
            try
            {
                type = (Class<Composite>) Class.forName( className );
                break;
            }
            catch( ClassNotFoundException e )
            {
                // Keep looking!
            }
        }

    }
}
