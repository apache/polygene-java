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

import org.qi4j.functional.Specification;

/**
 * Fluid API for declaring a layer in an application. This is obtained by calling {@link ApplicationAssembly#layer(String)}.
 */
public interface LayerAssembly
{
    /**
     * Get an assembly for a particular Module. If this is called many times with the same name, then the same module
     * is affected.
     *
     * @param name
     * @return
     */
    ModuleAssembly module( String name );

    ApplicationAssembly application();

    String name();

    LayerAssembly setName( String name );

    LayerAssembly setMetaInfo( Object info );

    LayerAssembly uses( LayerAssembly... layerAssembly );

    <ThrowableType extends Throwable> void visit( AssemblyVisitor<ThrowableType> visitor )
        throws ThrowableType;

    /**
     * Given a Specification for EntityAssembly's, returns a EntityDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification
     * @return
     */
    EntityDeclaration entities(Specification<? super EntityAssembly> specification);

    /**
     * Given a Specification for ServiceAssembly's, returns a ServiceDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification
     * @return
     */
    ServiceDeclaration services( Specification<? super ServiceAssembly> specification);

    /**
     * Given a Specification for TransientAssembly's, returns a TransientDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification
     * @return
     */
    TransientDeclaration transients( Specification<? super TransientAssembly> specification);

    /**
     * Given a Specification for ValueAssembly's, returns a ValueDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification
     * @return
     */
    ValueDeclaration values( Specification<? super ValueAssembly> specification);

    /**
     * Given a Specification for ObjectAssembly's, returns a ObjectDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification
     * @return
     */
    ObjectDeclaration objects(Specification<? super ObjectAssembly> specification);

    /**
     * Given a Specification for ImportedServiceAssembly's, returns a ImportedServiceDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification
     * @return
     */
    ImportedServiceDeclaration importedServices(Specification<? super ImportedServiceAssembly> specification);
}
