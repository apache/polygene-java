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

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * JAVADOC
 */
public final class EntityBuilderInstance<T>
    implements EntityBuilder<T>
{
    private static final Method IDENTITY_METHOD;
    private static final Method CREATE_METHOD;
    private static QualifiedName identityStateName;

    private final ModuleInstance moduleInstance;
    private final EntityModel entityModel;
    private final ModuleUnitOfWork uow;
    private final EntityStoreUnitOfWork store;
    private final IdentityGenerator identityGenerator;
    private final String identity;

    private final BuilderEntityState entityState;
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
        ModuleInstance moduleInstance, EntityModel entityModel, ModuleUnitOfWork uow, EntityStoreUnitOfWork store,
        IdentityGenerator identityGenerator, String identity )
    {
        this.moduleInstance = moduleInstance;
        this.entityModel = entityModel;
        this.uow = uow;
        this.store = store;
        this.identityGenerator = identityGenerator;
        this.identity = identity;

        if( identityStateName == null )
        {
            identityStateName = QualifiedName.fromMethod( IDENTITY_METHOD );
        }

        entityState = new BuilderEntityState( entityModel );
        prototypeInstance = entityModel.newInstance( uow, moduleInstance, EntityReference.NULL, entityState );
    }

    @SuppressWarnings( "unchecked" )
    public T prototype()
    {
        return prototypeInstance.<T>proxy();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        return prototypeInstance.newProxy( mixinType );
    }

    public T newInstance()
        throws LifecycleException
    {
        String identity;

        // Figure out whether to use given or generated identity
        EntityState newEntityState;
        if( identityGenerator != null )
        {
            Class compositeType = entityModel.type();
            identity = identityGenerator.generate( compositeType );
            newEntityState = entityModel.newEntityState( store, EntityReference.parseEntityReference( identity ));
        }
        else
        {
            identity = (String) entityState.getProperty( identityStateName );

            if (identity == null)
            {
                identity = this.identity;

                if (identity == null)
                    throw new ConstructionException("No identity set and no identity generator specified");

                newEntityState = entityModel.newEntityState( store, EntityReference.parseEntityReference( identity ));
            } else
            {
                newEntityState = entityModel.newEntityState( store, EntityReference.parseEntityReference( identity ));
            }

        }

        entityState.copyTo(newEntityState);

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

        // Add entity in UOW
        uow.addEntity( instance );

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
