/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 * Copyright 2012 Paul Merlin.
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

import org.qi4j.api.activation.Activator;
import org.qi4j.api.structure.Layer;
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
     * @param name The name of the Module to retrieve or create.
     *
     * @return The ModuleAssembly for the Module.
     */
    ModuleAssembly module( String name );

    ApplicationAssembly application();

    String name();

    LayerAssembly setName( String name );

    LayerAssembly setMetaInfo( Object info );

    LayerAssembly uses( LayerAssembly... layerAssembly );

    /**
     * Set the layer activators. Activators are executed in order around the
     * Layer activation and passivation.
     *
     * @param activators the layer activators
     * @return the assembly
     */    
    LayerAssembly withActivators( Class<? extends Activator<Layer>>... activators );

    <ThrowableType extends Throwable> void visit( AssemblyVisitor<ThrowableType> visitor )
        throws ThrowableType;

    /**
     * Given a Specification for EntityAssembly's, returns a EntityDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the EntityComposite types of interest.
     *
     * @return An EntityDeclaration for the specified EntityComposite types.
     */
    EntityDeclaration entities( Specification<? super EntityAssembly> specification );

    /**
     * Given a Specification for ServiceAssembly's, returns a ServiceDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the ServiceComposite types of interest.
     *
     * @return An ServiceDeclaration for the specified ServiceComposite types.
     */
    ServiceDeclaration services( Specification<? super ServiceAssembly> specification );

    /**
     * Given a Specification for TransientAssembly's, returns a TransientDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the TransientComposite types of interest.
     *
     * @return An TransientDeclaration for the specified TransientComposite types.
     */
    TransientDeclaration transients( Specification<? super TransientAssembly> specification );

    /**
     * Given a Specification for ValueAssembly's, returns a ValueDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the ValueComposite types of interest.
     *
     * @return An ValueDeclaration for the specified ValueComposite types.
     */
    ValueDeclaration values( Specification<? super ValueAssembly> specification );

    /**
     * Given a Specification for ObjectAssembly's, returns a ObjectDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the Object types of interest.
     *
     * @return An ObjectDeclaration for the specified Object types.
     */
    ObjectDeclaration objects( Specification<? super ObjectAssembly> specification );

    /**
     * Given a Specification for ImportedServiceAssembly's, returns a ImportedServiceDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the Imported Service types of interest.
     *
     * @return An ImportedServiceDeclaration for the specified Imported Service types.
     */
    ImportedServiceDeclaration importedServices( Specification<? super ImportedServiceAssembly> specification );
}
