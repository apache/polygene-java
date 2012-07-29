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

import org.qi4j.api.common.Visibility;

/**
 * Fluent API for declaring entities. Instances
 * of this API are acquired by calling {@link ModuleAssembly#entities(Class[])}.
 */
public interface EntityDeclaration
{
    /**
     * Set additional metainfo for this entity declaration.
     *
     * @param info metainfo that can be retrieved from the EntityDescriptor.
     *
     * @return This instance for a fluid DSL
     */
    EntityDeclaration setMetaInfo( Object info );

    /**
     * Set visibility for declared entities.
     *
     * @param visibility The {@link Visibility} that this EntityComposite will have.
     *
     * @return This instance for a fluid DSL
     */
    EntityDeclaration visibleIn( Visibility visibility );

    /**
     * Declare additional concerns for these entities.
     *
     * @param concerns The concerns that are to be added to the EntityComposite beyond the statically declared ones.
     *
     * @return This instance for a fluid DSL
     */
    EntityDeclaration withConcerns( Class<?>... concerns );

    /**
     * Declare additional side-effects for these entitites.
     *
     * @param sideEffects The sideeffects that are to be added to the EntityComposite beyond the statically declared ones.
     *
     * @return This instance for a fluid DSL
     */
    EntityDeclaration withSideEffects( Class<?>... sideEffects );

    /**
     * Declare additional mixins for these entities.
     * <p/>
     * This can be useful to override any default mixins from the entity interface.
     *
     * @param mixins The mixins that are to be added to the EntityComposite beyond the statically declared ones.
     *
     * @return This instance for a fluid DSL
     */
    EntityDeclaration withMixins( Class<?>... mixins );

    /**
     * Declare additional interfaces for these declared interfaces.
     * <p/>
     * This can be useful to add types that the entities should implement, but
     * which you do not want to include in the entity interface itself.
     *
     * @param types list of interfaces to add
     *
     * @return This instance for a fluid DSL
     */
    EntityDeclaration withTypes( Class<?>... types );
}
