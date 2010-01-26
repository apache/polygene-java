/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.spi.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.HashSet;

public final class MethodSet
    extends HashSet<Method>
    implements Externalizable
{
    static final long serialVersionUID = 1L;

    public void writeExternal( ObjectOutput out )
        throws IOException
    {
        out.writeInt( size() );
        for( Method method : this )
        {
            SerializationUtil.writeMethod( out, method );
        }
    }

    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        int size = in.readInt();
        for( int i = 0; i < size; i++ )
        {
            Method method = SerializationUtil.readMethod( in );
            add( method );
        }
    }
}
