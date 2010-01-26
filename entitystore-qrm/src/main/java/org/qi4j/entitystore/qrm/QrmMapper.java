package org.qi4j.entitystore.qrm;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;

/**
 * User: alex
 */
public interface QrmMapper
{

    void bootstrap( QrmEntityStoreDescriptor cfg );

    Class findMappedMixin( EntityDescriptor eDesc );

    String fetchNextId( Class mappedClassName );

    EntityDescriptor fetchDescriptor( Class mappedClazz );

    EntityState get( DefaultEntityStoreUnitOfWork unitOfWork, Class mappedClazz, EntityReference identity );

    boolean newEntity( Class mappedClazz, DefaultEntityState state, String version );

    boolean delEntity( Class mappedClazz, DefaultEntityState state, String version );

    boolean updEntity( Class mappedClazz, DefaultEntityState state, String version );
}
