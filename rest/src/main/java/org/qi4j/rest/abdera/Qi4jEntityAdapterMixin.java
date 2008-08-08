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

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Content;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.library.rdf.entity.EntitySerializer;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.structure.Module;
import org.qi4j.util.ClassUtil;


/**
 * TODO
 */
public class Qi4jEntityAdapterMixin
    extends AbstractEntityCollectionAdapter<EntityState>
    implements Activatable
{
    @Structure UnitOfWorkFactory uowf;
    @Structure Module module;

    @Service EntitySerializer serializer;
    @Service EntityStore store;

    @This Configuration<Qi4jEntityAdapterConfiguration> config;

    public void activate() throws Exception
    {
        setHref( config.configuration().href().get() );
    }

    public void passivate() throws Exception
    {
    }

    public EntityState postEntry( String s, IRI iri, String s1, Date date, List list, Content content, RequestContext requestContext ) throws ResponseContextException
    {
        return null;
    }

    public void deleteEntry( String s, RequestContext requestContext ) throws ResponseContextException
    {
    }

    public Object getContent( EntityState o, RequestContext requestContext ) throws ResponseContextException
    {
        Iterable<Statement> graph = serializer.serialize( o );

        try
        {
            StringWriter stringWriter = new StringWriter();
            new RdfXmlSerializer().serialize( graph, stringWriter );
            Content content = requestContext.getAbdera().getFactory().newContent( Content.Type.XML );
            content.setValue( stringWriter.toString() );
            return content;
        }
        catch( RDFHandlerException e )
        {
            throw new ResponseContextException( 500, e );
        }
    }

    public Iterable<EntityState> getEntries( RequestContext requestContext ) throws ResponseContextException
    {
        return store;
    }

    public EntityState getEntry( String s, RequestContext requestContext ) throws ResponseContextException
    {
        try
        {
            String[] qidString = s.split( "_" );
            QualifiedIdentity qid = new QualifiedIdentity( qidString[ 1 ], qidString[ 0 ] );
            return store.getEntityState( qid );
        }
        catch( EntityNotFoundException e )
        {
            throw new ResponseContextException( e.getMessage(), 404 );
        }
        catch( IllegalArgumentException e )
        {
            throw new ResponseContextException( 500, e );
        }
        catch( EntityStoreException e )
        {
            throw new ResponseContextException( 500, e );
        }
    }

    public String getId( EntityState o ) throws ResponseContextException
    {
        return o.qualifiedIdentity().toString();
    }

    public String getName( EntityState o ) throws ResponseContextException
    {
        return ClassUtil.normalizeClassToURI( o.qualifiedIdentity().type() ) + "_" + o.qualifiedIdentity().identity();
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
