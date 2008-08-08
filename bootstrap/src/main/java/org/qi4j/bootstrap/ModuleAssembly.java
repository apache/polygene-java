/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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

package org.qi4j.bootstrap;

import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.service.ServiceComposite;

public interface ModuleAssembly
{
    void addAssembler( Assembler assembler )
        throws AssemblyException;

    LayerAssembly getLayerAssembly();

    void setName( String name );

    String name();

    CompositeDeclaration addComposites( Class<? extends Composite>... compositeTypes )
        throws AssemblyException;

    EntityDeclaration addEntities( Class<? extends EntityComposite>... compositeTypes )
        throws AssemblyException;

    ObjectDeclaration addObjects( Class... objectTypes )
        throws AssemblyException;

    ServiceDeclaration addServices( Class<? extends ServiceComposite>... serviceTypes )
        throws AssemblyException;

    <T> InfoDeclaration<T> on( Class<T> mixinType );

}
