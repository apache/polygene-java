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
package org.apache.polygene.demo.intro;

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;

public class WhatsAnObjectDocs
{
        // START SNIPPET: wo1
        @Mixins( SomeMixin.class )
        interface MyEntity extends Some, Other, HasIdentity {}
        // END SNIPPET: wo1

        // START SNIPPET: wo2
        interface SomeState
        {
            Property<String> someProperty();
        }
        // END SNIPPET: wo2

        // START SNIPPET: wo3
        interface MyState extends SomeState, OtherState //, ...
        {}
        // END SNIPPET: wo3


        abstract class SomeMixin implements Some
        {}

        interface Some
        {}

        interface Other
        {}

        interface OtherState
        {}

        {
            UnitOfWork uow = null;
            // START SNIPPET: wo4
            EntityBuilder<MyEntity> builder = uow.newEntityBuilder(MyEntity.class);
            MyState state = builder.instanceFor(MyState.class);

            //... init state ...

            MyEntity instance = builder.newInstance();
            // END SNIPPET: wo4
        }

    }
