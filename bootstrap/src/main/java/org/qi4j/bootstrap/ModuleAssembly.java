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
import org.qi4j.functional.Specification;

/**
 * The ModuleAssembly is used to register any information about
 * what the module should contain, such as composites, entities and services.
 * <p/>
 * Use the methods and the fluent API's to declare how the module should be constructed.
 */
public interface ModuleAssembly
{
   /**
    * Access the layer assembly for this module.
    *
    * @return
    */
   LayerAssembly layer();

   /**
    * Set the name of this module.
    *
    * @param name
    * @return
    */
   ModuleAssembly setName(String name);

   /**
    * Access the currently set name for this module.
    *
    * @return
    */
   String name();

   /**
    * Declare a list of TransientComposites for this Module. Use the TransientDeclaration that is returned to
    * declare further settings. Note that the TransientDeclaration works on all of the types specified.
    *
    * @param compositeTypes
    * @return
    */
   TransientDeclaration transients(Class<?>... compositeTypes);

   /**
    * Given a Specification for TransientAssembly's, returns a TransientDeclaration that can
    * be used to work with all of the assemblies matched by the specification.
    *
    * @param specification
    * @return
    */
   TransientDeclaration transients(Specification<? super TransientAssembly> specification);

   /**
    * Declare a list of ValueComposites for this Module. Use the ValueDeclaration that is returned to
    * declare further settings. Note that the ValueDeclaration works on all of the types specified.
    *
    * @param compositeTypes
    * @return
    */
   ValueDeclaration values(Class<?>... compositeTypes);

   /**
    * Given a Specification for ValueAssembly's, returns a ValueDeclaration that can
    * be used to work with all of the assemblies matched by the specification.
    *
    * @param specification
    * @return
    */
   ValueDeclaration values(Specification<? super ValueAssembly> specification);

   /**
    * Declare a list of EntityComposites for this Module. Use the EntityDeclaration that is returned to
    * declare further settings. Note that the EntityDeclaration works on all of the types specified.
    *
    * @param compositeTypes
    * @return
    */
   EntityDeclaration entities(Class<?>... compositeTypes);

   /**
    * Given a Specification for EntityAssembly's, returns a EntityDeclaration that can
    * be used to work with all of the assemblies matched by the specification.
    *
    * @param specification
    * @return
    */
   EntityDeclaration entities(Specification<? super EntityAssembly> specification);

   /**
    * Declare a list of object classes for this Module. Use the ObjectDeclaration that is returned to
    * declare further settings. Note that the ObjectDeclaration works on all of the types specified.
    *
    * @param objectTypes
    * @return
    */
   ObjectDeclaration objects(Class<?>... objectTypes);

   /**
    * Given a Specification for ObjectAssembly's, returns a ObjectDeclaration that can
    * be used to work with all of the assemblies matched by the specification.
    *
    * @param specification
    * @return
    */
   ObjectDeclaration objects(Specification<? super ObjectAssembly> specification);

   /**
    * Create a list of ServiceComposites for this Module. Use the ServiceDeclaration that is returned to
    * declare further settings. This will always create new assemblies for the specified types, instead
    * of potentially working on already declared types like the services(...) method.
    *
    *
    * @param serviceTypes
    * @return
    */
   ServiceDeclaration addServices(Class<?>... serviceTypes);

   /**
    * Declare a list of ServiceComposites for this Module. Use the ServiceDeclaration that is returned to
    * declare further settings. Note that the ServiceDeclaration works on all of the types specified.
    *
    * @param serviceTypes
    * @return
    */
   ServiceDeclaration services(Class<?>... serviceTypes);

   /**
    * Given a Specification for ServiceAssembly's, returns a ServiceDeclaration that can
    * be used to work with all of the assemblies matched by the specification.
    *
    * @param specification
    * @return
    */
   ServiceDeclaration services(Specification<? super ServiceAssembly> specification);

   /**
    * Declare a list of imported services for this Module. Use the ImportedServiceDeclaration that is returned to
    * declare further settings. Note that the ImportedServiceDeclaration works on all of the types specified.
    *
    * @param serviceTypes
    * @return
    */
   ImportedServiceDeclaration importedServices(Class<?>... serviceTypes);

   /**
    * Given a Specification for ImportedServiceAssembly's, returns a ImportedServiceDeclaration that can
    * be used to work with all of the assemblies matched by the specification.
    *
    * @param specification
    * @return
    */
   ImportedServiceDeclaration importedServices(Specification<? super ImportedServiceAssembly> specification);

   <T> MixinDeclaration<T> forMixin(Class<T> mixinType);

   public <ThrowableType extends Throwable> void visit(AssemblyVisitor<ThrowableType> visitor)
           throws ThrowableType;
}
