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

package org.qi4j.runtime.unitofwork;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.*;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.api.entity.EntityReference;

/**
 * JAVADOC
 */
public final class EntityBuilderInstance<T>
    implements EntityBuilder<T>
{
    private static final Method IDENTITY_METHOD;
    private static final Method CREATE_METHOD;
    private static StateName identityStateName;

    private final ModuleInstance moduleInstance;
    private final EntityModel entityModel;
    private final ModuleUnitOfWork uow;
    private final EntityStore store;
    private final IdentityGenerator identityGenerator;

    private final DefaultDiffEntityState entityState;
    private final EntityInstance prototypeInstance;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
            CREATE_METHOD = Lifecycle.class.getMethod( "create" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: EntityBuilderInstance" );
        }
    }

    public EntityBuilderInstance(
        ModuleInstance moduleInstance, EntityModel entityModel, ModuleUnitOfWork uow, EntityStore store,
        IdentityGenerator identityGenerator )
    {
        this.moduleInstance = moduleInstance;
        this.entityModel = entityModel;
        this.uow = uow;
        this.store = store;
        this.identityGenerator = identityGenerator;

        if (identityStateName == null)
            identityStateName = entityModel.state().<PropertyTypeDescriptor>getPropertyByQualifiedName(QualifiedName.fromMethod(IDENTITY_METHOD)).propertyType().stateName();

        entityState = new DefaultDiffEntityState();
        entityState.addEntityTypeReference(entityModel.entityType().reference());
        prototypeInstance = entityModel.newInstance(uow, moduleInstance, EntityReference.NULL, entityState);
    }

    public EntityBuilderInstance( ModuleInstance moduleInstance, EntityModel model, ModuleUnitOfWork uow, EntityStore store, String identity )
    {
        this(moduleInstance, model, uow, store, (IdentityGenerator) null);
        entityState.setProperty(identityStateName, '\"'+identity+'\"');
    }

    @SuppressWarnings( "unchecked" )
    public T prototype()
    {
        return prototypeInstance.<T>proxy();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        return prototypeInstance.newProxy(mixinType);
    }

    public T newInstance()
        throws LifecycleException
    {
        String identity;
        String identityJson;

        // Figure out whether to use given or generated identity
        EntityState newEntityState;
        if( identityGenerator != null )
        {
            Class compositeType = entityModel.type();
            identity = identityGenerator.generate( compositeType );
            identityJson = '\"'+identity+'\"';
            newEntityState = entityModel.newEntityState( store, EntityReference.parseEntityReference(identity), moduleInstance.layerInstance().applicationInstance().runtime());
        } else
        {
            identityJson = entityState.getProperty(identityStateName);
            identity = identityJson.substring(1, identityJson.length()-2);
            newEntityState = entityModel.newEntityState( store, EntityReference.parseEntityReference(identity), moduleInstance.layerInstance().applicationInstance().runtime());
        }

        // Transfer state from prototype
        entityState.applyTo(newEntityState);

        // Set identity property
        newEntityState.setProperty(identityStateName, identityJson);

        EntityInstance instance = entityModel.newInstance( uow, moduleInstance, newEntityState.identity(), newEntityState );

        Object proxy = instance.proxy();

        // Invoke lifecycle create() method
        if( instance.entityModel().hasMixinType( Lifecycle.class ) )
        {
            try
            {
                instance.invoke( null, CREATE_METHOD, new Object[0] );
            }
            catch( LifecycleException throwable )
            {
                throw throwable;
            }
            catch( Throwable throwable )
            {
                throw new LifecycleException( throwable );
            }
        }

        // Check constraints
        instance.checkConstraints();

        // Register entity in UOW
        uow.instance().createEntity( instance, store );

        return (T) proxy;
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                T instance = newInstance();
                return instance;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
