/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import java.lang.reflect.Method;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.composite.AbstractMixinsModel;
import org.qi4j.runtime.composite.MixinDeclaration;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.UsesInstance;

/**
 * TODO
 */
public final class EntityMixinsModel extends AbstractMixinsModel
{
    private EntityStateModel entityStateModel;

    public EntityMixinsModel( Class<? extends Composite> compositeType, EntityStateModel entityStateModel )
    {
        super( compositeType );
        this.entityStateModel = entityStateModel;
        mixins.add( new MixinDeclaration( EntityMixin.class, Entity.class ) );
    }

    @Override public MixinModel implementMethod( Method method )
    {
        entityStateModel.addStateFor( method );

        return super.implementMethod( method );
    }

    public Object newMixin( EntityInstance entityInstance, StateHolder state, Object[] mixins, Method method )
    {
        MixinModel model = methodImplementation.get( method );
        Object mixin = model.newInstance( entityInstance, UsesInstance.NO_USES, state );
        mixins[methodIndex.get( method )] = mixin;
        return mixin;
    }
}
