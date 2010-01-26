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

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueComposite;

/**
 * The ModuleAssembly is used to register any information about
 * what the module should contain, such as composites, entities and services.
 *
 * Use the methods and the fluent API's to declare how the module should be constructed.
 */
public interface ModuleAssembly
{
    LayerAssembly layerAssembly();

    ModuleAssembly setName( String name );

    String name();

    TransientDeclaration addTransients( Class<? extends TransientComposite>... compositeTypes )
        throws AssemblyException;

    ValueDeclaration addValues( Class<? extends ValueComposite>... compositeTypes )
        throws AssemblyException;

    EntityDeclaration addEntities( Class<? extends EntityComposite>... compositeTypes )
        throws AssemblyException;

    ObjectDeclaration addObjects( Class... objectTypes )
        throws AssemblyException;

    ServiceDeclaration addServices( Class<? extends ServiceComposite>... serviceTypes )
        throws AssemblyException;

    ImportedServiceDeclaration importServices( Class... serviceTypes )
        throws AssemblyException;

    <T> MixinDeclaration<T> forMixin( Class<T> mixinType );

    public void visit( AssemblyVisitor visitor )
        throws AssemblyException;
}
