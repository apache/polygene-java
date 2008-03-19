package org.qi4j.entity.index.rdf;

import org.qi4j.composite.Mixins;
import org.qi4j.service.ServiceComposite;

@Mixins( RDFIndexerMixin.class )
public interface RDFIndexerComposite
    extends Indexer, ServiceComposite
{

}
