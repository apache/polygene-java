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

package org.qi4j.rest.abdera;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Content;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.library.rdf.entity.EntitySerializer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.structure.Module;

/**
 * TODO
 */
public class Qi4jEntityAdapter
    extends AbstractEntityCollectionAdapter<EntityState>
{
    @Structure UnitOfWorkFactory uowf;
    @Structure Module module;

    @Service EntitySerializer serializer;
    @Service EntityStore store;

    public EntityState postEntry( String s, IRI iri, String s1, Date date, List list, Content content, RequestContext requestContext ) throws ResponseContextException
    {
        return null;
    }

    public void deleteEntry( String s, RequestContext requestContext ) throws ResponseContextException
    {
    }

    public Object getContent( EntityState o, RequestContext requestContext ) throws ResponseContextException
    {
        return serializer.serialize( o.qualifiedIdentity() );
    }

    public Iterable<EntityState> getEntries( RequestContext requestContext ) throws ResponseContextException
    {
        UnitOfWork unitOfWork = uowf.currentUnitOfWork();
        List entities = new ArrayList<EntityComposite>();
        for( EntityComposite entityComposite : unitOfWork.queryBuilderFactory().newQueryBuilder( EntityComposite.class ).newQuery() )
        {
            entities.add( entityComposite );
        }
        return entities;
    }

    public EntityState getEntry( String s, RequestContext requestContext ) throws ResponseContextException
    {
        QualifiedIdentity qid = new QualifiedIdentity( requestContext.getTargetPath() );
        return store.getEntityState( null, qid );
    }

    public String getId( EntityState o ) throws ResponseContextException
    {
        return o.qualifiedIdentity().identity();
    }

    public String getName( EntityState o ) throws ResponseContextException
    {
        return o.qualifiedIdentity().identity();
    }

    public String getTitle( EntityState o ) throws ResponseContextException
    {
        return o.qualifiedIdentity().identity();
    }

    public Date getUpdated( EntityState o ) throws ResponseContextException
    {
        return new Date( o.lastModified() );
    }

    public void putEntry( EntityState o, String s, Date date, List list, String s1, Content content, RequestContext requestContext ) throws ResponseContextException
    {
    }

    public String getAuthor( RequestContext requestContext ) throws ResponseContextException
    {
        return "Qi4j";
    }

    public String getId( RequestContext requestContext )
    {
        return "tag:qi4j.org,2008:entity:feed";
    }

    public String getTitle( RequestContext requestContext )
    {
        return "Qi4j Entities";
    }
}
