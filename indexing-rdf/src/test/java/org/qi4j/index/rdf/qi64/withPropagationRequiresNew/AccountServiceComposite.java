package org.qi4j.index.rdf.qi64.withPropagationRequiresNew;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkConcern;

@Concerns( UnitOfWorkConcern.class )
public interface AccountServiceComposite
    extends ServiceComposite, AccountService
{
}
