package org.qi4j.demo.intro;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;

public class WhatsAnObjectDocs
{
        // START SNIPPET: wo1
        @Mixins(SomeMixin.class)
        interface MyEntity
                extends Some, Other, EntityComposite
        {}
// END SNIPPET: wo1

        // START SNIPPET: wo2
        interface SomeState
        {
            Property<String> someProperty();
        }
// END SNIPPET: wo2

        // START SNIPPET: wo3
        interface MyState
                extends SomeState, OtherState //, ...
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
// END SNIPPET: wo4        }


        }

    }
