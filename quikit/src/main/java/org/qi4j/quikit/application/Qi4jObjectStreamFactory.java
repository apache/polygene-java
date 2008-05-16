/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.quikit.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.apache.wicket.util.io.IObjectStreamFactory;
import org.qi4j.Qi4j;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.spi.serialization.CompositeInputStream;
import org.qi4j.spi.serialization.CompositeOutputStream;

/**
 * @author edward.yakop@gmail.com
 */
public final class Qi4jObjectStreamFactory
    implements IObjectStreamFactory
{
    @Structure
    private CompositeBuilderFactory compositeBuilderFactory;

    @Structure
    private Qi4j qi4j;

    public final ObjectInputStream newObjectInputStream( InputStream in )
        throws IOException
    {
        return new CompositeInputStream( in, compositeBuilderFactory, qi4j );
    }

    public final ObjectOutputStream newObjectOutputStream( OutputStream out )
        throws IOException
    {
        return new CompositeOutputStream( out );
    }
}
