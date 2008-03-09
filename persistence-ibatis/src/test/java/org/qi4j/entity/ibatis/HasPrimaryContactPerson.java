package org.qi4j.entity.ibatis;

import org.qi4j.association.Association;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.AssociationField;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( HasPrimaryContactPerson.HasPrimaryContactPersonMixin.class )
public interface HasPrimaryContactPerson
{
    Association<PersonComposite> primaryContactPerson();

    class HasPrimaryContactPersonMixin implements HasPrimaryContactPerson
    {
        @AssociationField
        private Association<PersonComposite> primaryContactPerson;

        public Association<PersonComposite> primaryContactPerson()
        {
            return primaryContactPerson;
        }
    }
}
