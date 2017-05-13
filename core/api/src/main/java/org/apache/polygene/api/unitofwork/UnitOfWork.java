/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.unitofwork;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.AmbiguousTypeException;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.entity.LifecycleException;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.structure.MetaInfoHolder;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.api.value.ValueBuilder;

/**
 * All operations on entities goes through an UnitOfWork.
 * <p>
 * A UnitOfWork allows you to access
 * Entities and work with them. All modifications to Entities are recorded by the UnitOfWork,
 * and at the end they may be sent to the underlying EntityStore by calling complete(). If the
 * UoW was read-only you may instead simply discard() it.
 * </p>
 * <p>
 * A UoW differs from a traditional Transaction in the sense that it is not tied at all to the underlying
 * storage resource. Because of this there is no timeout on a UoW. It can be very short or very long.
 * Another difference is that if a call to complete() fails, and the cause is validation errors in the
 * Entities of the UoW, then these can be corrected and the UoW retried. By contrast, when a Transaction
 * commit fails, then the whole transaction has to be done from the beginning again.
 * </p>
 * <p>
 * A UoW can be associated with a Usecase. A Usecase describes the metainformation about the process
 * to be performed by the UoW.
 * </p>
 * <p>
 * If a code block that uses a UoW throws an exception you need to ensure that this is handled properly,
 * and that the UoW is closed before returning. Because discard() is a no-op if the UoW is closed, we therefore
 * recommend the following template to be used:
 * </p>
 * <pre>
 *     UnitOfWork uow = module.newUnitOfWork();
 *     try
 *     {
 *         ...
 *         uow.complete();
 *     }
 *     finally
 *     {
 *         uow.discard();
 *     }
 * </pre>
 * <p>
 * This ensures that in the happy case the UoW is completed, and if any exception is thrown the UoW is discarded. After
 * the UoW has completed the discard() method doesn't do anything, and so has no effect. You can choose to either add
 * catch blocks for any exceptions, including exceptions from complete(), or skip them.
 * </p>
 * <p>
 * Since 2.1 you can leverage Java 7 Automatic Resource Management (ie. Try With Resources) and use the following
 * template instead:
 * </p>
 * <pre>
 *     try( UnitOfWork uow = module.newUnitOfWork() )
 *     {
 *         ...
 *         uow.complete();
 *     }
 * </pre>
 * <p>
 * It has the very same effect as the template above but is shorter.</p>
 */
public interface UnitOfWork extends MetaInfoHolder, AutoCloseable
{

    /**
     * Get the UnitOfWorkFactory that this UnitOfWork was created from.
     *
     * @return The UnitOfWorkFactory instance that was used to create this UnitOfWork.
     */
    UnitOfWorkFactory unitOfWorkFactory();

    /**
     * Current Time is a relative concept in systems capable of event sourcing and other
     * history-capable systems. Current time is always expected to be read from here, and
     * history-capable systems will set the current time for each {@link UnitOfWorkFactory#newUnitOfWork(Instant)}
     *
     * @return the current time, either actual real time or historical time being part of some playback.
     */
    Instant currentTime();

    /**
     * Get the Usecase for this UnitOfWork
     *
     * @return the Usecase
     */
    Usecase usecase();

    /**
     * Sets an arbitrary metaInfo object on this {@code UnitOfWork} which can be used for application-specific
     * information.
     * <p>
     * The metaInfo object is retrieved by the {@link UnitOfWork#metaInfo(Class)} method on the same UnitOfWork
     * instance.
     * </p>
     *
     * @param metaInfo The metaInfo object that can be retrieved with {@link UnitOfWork#metaInfo(Class)}.
     */
    void setMetaInfo( Object metaInfo );

    /**
     * Creates a {@link Query} from the given {@link QueryBuilder} on this {@code UnitOfWork}.
     *
     * @param queryBuilder The QueryBuilder holding the query specification/expression
     * @param <T>          The resulting type of the query.
     * @return A Query against this {@code UnitOfWork}
     */
    <T> Query<T> newQuery( QueryBuilder<T> queryBuilder );

    /**
     * Create a new Entity which implements the given mixin type.
     * <p>
     * An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * EntityComposites implement the type then an AmbiguousTypeException will be thrown.
     * </p>
     * <p>
     * The reference of the Entity will be generated by the IdentityGenerator of the Module of the EntityComposite.
     * </p>
     *
     * @param <T>  Entity type
     * @param type the mixin type that the EntityComposite must implement
     * @return a new Entity
     * @throws NoSuchEntityTypeException if no EntityComposite type of the given mixin type has been registered
     * @throws AmbiguousTypeException    If several mixins implement the given type
     * @throws LifecycleException        if the entity cannot be created
     */
    <T> T newEntity( Class<T> type )
        throws NoSuchEntityTypeException, AmbiguousTypeException, LifecycleException;

    /**
     * Create a new Entity which implements the given mixin type. An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * EntityComposites implement the type then an AmbiguousTypeException will be thrown.
     *
     * @param <T>      Entity type
     * @param type     the mixin type that the EntityComposite must implement
     * @param identity the reference of the new Entity
     * @return a new Entity
     * @throws NoSuchEntityTypeException if no EntityComposite type of the given mixin type has been registered
     * @throws AmbiguousTypeException    If several mixins implement the given type
     * @throws LifecycleException        if the entity cannot be created
     */
    <T> T newEntity( Class<T> type, @Optional Identity identity )
        throws NoSuchEntityTypeException, AmbiguousTypeException, LifecycleException;

    /**
     * Create a new EntityBuilder for an EntityComposite which implements the given mixin type. An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * EntityComposites implement the type then an AmbiguousTypeException will be thrown.
     *
     * @param <T>  Entity type
     * @param type the mixin type that the EntityComposite must implement
     * @return a new EntityBuilder
     * @throws NoSuchEntityTypeException if no EntityComposite type of the given mixin type has been registered
     * @throws AmbiguousTypeException    If several mixins implement the given type
     */
    <T> EntityBuilder<T> newEntityBuilder( Class<T> type )
        throws NoSuchEntityTypeException, AmbiguousTypeException;

    /**
     * Create a new EntityBuilder for an EntityComposite which implements the given mixin type. An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * mixins implement the type then an AmbiguousTypeException will be thrown.
     *
     * @param <T>      Entity type
     * @param type     the mixin type that the EntityComposite must implement
     * @param identity the reference of the new Entity
     * @return a new EntityBuilder
     * @throws NoSuchEntityTypeException if no EntityComposite type of the given mixin type has been registered
     * @throws AmbiguousTypeException    If several mixins implement the given type
     */
    <T> EntityBuilder<T> newEntityBuilder( Class<T> type, @Optional Identity identity )
        throws NoSuchEntityTypeException, AmbiguousTypeException;

    /**
     * Create a new EntityBuilder for an EntityComposite wich implements the given mixin type starting with the given
     * state.
     * <p>
     * An EntityComposite will be chosen according to what has been registered and the visibility rules for Modules and
     * Layers will be considered.
     *
     * @param <T>                      Entity type
     * @param type                     Entity type
     * @param propertyFunction         a function providing the state of properties
     * @param associationFunction      a function providing the state of associations
     * @param manyAssociationFunction  a function providing the state of many associations
     * @param namedAssociationFunction a function providing the state of named associations
     * @return a new EntityBuilder starting with the given state
     * @throws NoSuchEntityTypeException if no EntityComposite type of the given mixin type has been registered
     * @throws AmbiguousTypeException    If several mixins implement the given type
     */
    <T> EntityBuilder<T> newEntityBuilderWithState( Class<T> type,
                                                    Function<PropertyDescriptor, Object> propertyFunction,
                                                    Function<AssociationDescriptor, EntityReference> associationFunction,
                                                    Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction,
                                                    Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction
                                                  )
        throws NoSuchEntityTypeException, AmbiguousTypeException;

    /**
     * Create a new EntityBuilder for an EntityComposite wich implements the given mixin type starting with the given
     * state.
     * <p>
     * An EntityComposite will be chosen according to what has been registered and the visibility rules for Modules and
     * Layers will be considered.
     *
     * @param <T>                      Entity type
     * @param type                     Entity type
     * @param identity                 the reference of the new Entity
     * @param propertyFunction         a function providing the state of properties
     * @param associationFunction      a function providing the state of associations
     * @param manyAssociationFunction  a function providing the state of many associations
     * @param namedAssociationFunction a function providing the state of named associations
     * @return a new EntityBuilder starting with the given state
     * @throws NoSuchEntityTypeException If no mixins implements the given type
     * @throws AmbiguousTypeException    If several mixins implement the given type
     */
    <T> EntityBuilder<T> newEntityBuilderWithState( Class<T> type, @Optional Identity identity,
                                                    Function<PropertyDescriptor, Object> propertyFunction,
                                                    Function<AssociationDescriptor, EntityReference> associationFunction,
                                                    Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction,
                                                    Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction
                                                  )
        throws NoSuchEntityTypeException, AmbiguousTypeException;

    /**
     * Find an Entity of the given mixin type with the give reference. This
     * method verifies that it exists by asking the underlying EntityStore.
     *
     * @param <T>      Entity type
     * @param type     of the entity
     * @param identity of the entity
     * @return the entity
     * @throws NoSuchEntityTypeException if no entity type could be found
     * @throws NoSuchEntityException     if the entity could not be found
     */
    <T> T get( Class<T> type, Identity identity )
        throws NoSuchEntityTypeException, NoSuchEntityException;

    /**
     * If you have a reference to an Entity from another
     * UnitOfWork and want to create a reference to it in this
     * UnitOfWork, then call this method.
     *
     * @param <T>    Entity type
     * @param entity the Entity to be dereferenced
     * @return an Entity from this UnitOfWork
     * @throws NoSuchEntityTypeException if no entity type could be found
     */
    <T> T get( T entity )
        throws NoSuchEntityTypeException;

    /**
     * Remove the given Entity.
     *
     * @param entity the Entity to be removed.
     * @throws LifecycleException if the entity could not be removed
     */
    void remove( Object entity )
        throws LifecycleException;

    /**
     * Complete this UnitOfWork. This will send all the changes down to the underlying
     * EntityStore's.
     *
     * @throws UnitOfWorkCompletionException         if the UnitOfWork could not be completed
     * @throws ConcurrentEntityModificationException if entities have been modified by others
     */
    void complete()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException;

    /**
     * Discard this UnitOfWork. Use this if a failure occurs that you cannot handle,
     * or if the usecase was of a read-only character. This is a no-op of the UnitOfWork
     * is already closed.
     */
    void discard();

    /**
     * Discard this UnitOfWork. Use this if a failure occurs that you cannot handle,
     * or if the usecase was of a read-only character. This is a no-op of the UnitOfWork
     * is already closed. This simply call the {@link #discard()} method and is an
     * implementation of the {@link AutoCloseable} interface providing Try With Resources
     * support for UnitOfWork.
     */
    @Override
    void close();

    /**
     * Check if the UnitOfWork is open. It is closed after either complete() or discard()
     * methods have been called successfully.
     *
     * @return true if the UnitOfWork is open.
     */
    boolean isOpen();

    /**
     * Check if the UnitOfWork is paused. It is not paused after it has been create through the
     * UnitOfWorkFactory, and it can be paused by calling {@link #pause()} and then resumed by calling
     * {@link #resume()}.
     *
     * @return true if this UnitOfWork has been paused.
     */
    boolean isPaused();

    /**
     * Pauses this UnitOfWork.
     * <p>
     * Calling this method will cause the underlying UnitOfWork to become the current UnitOfWork until the
     * the resume() method is called. It is the client's responsibility not to drop the reference to this
     * UnitOfWork while being paused.
     * </p>
     */
    void pause();

    /**
     * Resumes this UnitOfWork to again become the current UnitOfWork.
     */
    void resume();

    /**
     * Register a callback. Callbacks are invoked when the UnitOfWork
     * is completed or discarded.
     *
     * @param callback a callback to be registered with this UnitOfWork
     */
    void addUnitOfWorkCallback( UnitOfWorkCallback callback );

    /**
     * Unregister a callback. Callbacks are invoked when the UnitOfWork
     * is completed or discarded.
     *
     * @param callback a callback to be unregistered with this UnitOfWork
     */
    void removeUnitOfWorkCallback( UnitOfWorkCallback callback );

    /**
     * Converts the provided Entity to a Value of the same type.
     * This is a convenience method to convert an EntityComposite to a ValueComposite.
     * <p>
     * All Property values are transferred across as-is, and the Association, ManyAssociation
     * and NamedAssociatino values are kept in the ValueComposite as EntityReferences
     * until they are dereferenced (get() and other methods), and IF a UnitOfWork is
     * present at dereferencing the corresponding EntityCompoiste is retrieved from the
     * EntityStore. If there is not an UnitOfWork present, an exception is thrown.
     * </p>
     * <p>
     * For this to work, the Composites (both Entity and Value) must not declare the
     * EntityComposite and ValueComposite super types, but rely on the declaration in
     * the assembly, and also extend the Identity supertype.
     * </p>
     * Example;
     * <pre><code>
     *     public interface Person extends Identity { ... };
     *     public class MyAssembler
     *     {
     *         public void assemble( ModuleAssembly module )
     *         {
     *             module.values( Person.class );
     *             module.entities( Person.class );
     *         }
     *     }
     * </code></pre>
     *
     * @param <T>             The generic shared type
     * @param primaryType     The shared type for which the properties and associations will
     *                        be converted. Properties outside this type will be ignored.
     * @param entityComposite The entity to be convered.
     * @return The Value
     */
    <T extends HasIdentity> T toValue( Class<T> primaryType, T entityComposite );

    /**
     * Converts all the entities referenced in the ManyAssociation into a List of values of the same type.
     * <p>
     * All the referenced entities inside the association will be fetched from the underlying entity store,
     * which is potentially very expensive operation. Each of the fetched entities will be passed to
     * {@link #toValue(Class, HasIdentity)}, and its associations will NOT be converted into values, but remain
     * {@link EntityReference} values. Hence there is no problem with circular references.
     * </p>
     * <p>
     * For this to work, the type &lt;T&gt; must be registered at bootstrap as both an Entity and a Value, and
     * as seen in the method signature, also be sub-type of {@link HasIdentity}.
     * </p>
     *
     * @param association The association of entities to be converted into values.
     * @param <T>         The primary type of the association.
     * @return A List of ValueComposites that has been converted from EntityComposites referenced by the Associations.
     * @see #toValue(Class, HasIdentity)
     */
    <T extends HasIdentity> List<T> toValueList( ManyAssociation<T> association );

    /**
     * Converts all the entities referenced in the ManyAssociation into a Set of values of the same type.
     * <p>
     * All the referenced entities inside the association will be fetched from the underlying entity store,
     * which is potentially very expensive operation. However, any duplicate EntityReferences in the association
     * will be dropped before the fetch occurs. Each of the fetched entities will be passed to
     * {@link #toValue(Class, HasIdentity)}, and its associations will NOT be converted into values, but remain
     * {@link EntityReference} values. Hence there is no problem with circular references.
     * </p>
     * <p>
     * For this to work, the type &lt;T&gt; must be registered at bootstrap as both an Entity and a Value, and
     * as seen in the method signature, also be sub-type of {@link HasIdentity}.
     * </p>
     *
     * @param association The association of entities to be converted into values.
     * @param <T>         The primary type of the association.
     * @return A List of ValueComposites that has been converted from EntityComposites referenced by the Associations.
     * @see #toValue(Class, HasIdentity)
     */
    <T extends HasIdentity> Set<T> toValueSet( ManyAssociation<T> association );

    /**
     * Converts the {@link NamedAssociation} into a Map with a String key and a ValueComposite as the value.
     * <p>
     * A {@link NamedAssociation} is effectively a Map with a String key and an EntityReference as the value. The
     * EntityReference is fetched from the entity store and converted into a value of the same type.Each of the fetched
     * entities will be passed to {@link #toValue(Class, HasIdentity)}, and its associations will NOT be converted into
     * values, but remain {@link EntityReference} values. Hence there is no problem with circular references.
     * </p>
     * <p>
     * For this to work, the type &lt;T&gt; must be registered at bootstrap as both an Entity and a Value, and
     * as seen in the method signature, also be sub-type of {@link HasIdentity}.
     * </p>
     *
     * @param association The association of entities to be converted into values.
     * @param <T>         The primary type of the association.
     * @return A List of ValueComposites that has been converted from EntityComposites referenced by the Associations.
     * @see #toValue(Class, HasIdentity)
     */
    <T extends HasIdentity> Map<String, T> toValueMap( NamedAssociation<T> association );

    /**
     * Converts the provided Value to an Entity of the same type.
     * This is a convenience method to convert a ValueComposite to an EntityComposite.
     * <p>
     * All Property values are transferred across as-is (no deep copy in case mutable
     * types (DISCOURAGED!) are used), and the Association, ManyAssociation
     * and NamedAssociatino that were in the ValueComposite as EntityReferences are
     * transferred into the EntityComposite correctly, and can be dereferenced.
     * </p>
     * <p>
     * This method MUST be called within a UnitOfWork.
     * </p>
     * <p>
     * If an Entity with the Identity in the ValueComposite already exists, then that
     * Entity is updated with the values from the ValueComposite. If an Entity of
     * that Identity doesn't exist a new one is created.
     * </p>
     * <p>
     * For this to work, the Composites (both Entity and Value) must not declare the
     * EntityComposite and ValueComposite super types, but rely on the declaration in
     * the assembly, and also extend the Identity supertype.
     * </p>
     * Example;
     * <pre><code>
     *     public interface Person extends Identity { ... };
     *     public class MyAssembler
     *     {
     *         public void assemble( ModuleAssembly module )
     *         {
     *             module.values( Person.class );
     *             module.entities( Person.class );
     *         }
     *     }
     * </code></pre>
     *
     * @param <T>            The generic shared type
     * @param primaryType    The shared type for which the properties and associations will
     *                       be converted. Properties outside this type will be ignored.
     * @param valueComposite The Value to be convered into an Entity.
     * @return The new or updated Entity
     */
    <T extends HasIdentity> T toEntity( Class<T> primaryType, T valueComposite );

    /**
     * The Module of the UnitOfWork is defined as the Module the UnitOfWorkFactory belonged to from where the
     * UnitOfWork was created.
     *
     * @return the Module where this UnitOfWork was initialized.
     */
    ModuleDescriptor module();
}
