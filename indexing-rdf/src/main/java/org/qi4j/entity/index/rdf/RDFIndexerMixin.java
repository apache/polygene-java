package org.qi4j.entity.index.rdf;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedState;

public class RDFIndexerMixin
    implements Indexer
{

    private Repository repository;
    ;

    public RDFIndexerMixin()
    {
        repository = new SailRepository( new MemoryStore() );
        try
        {
            repository.initialize();
        }
        catch( RepositoryException e )
        {
            e.printStackTrace();
        }
    }

    public void index( final Map<SerializedEntity, SerializedState> newEntities,
                       final Map<SerializedEntity, SerializedState> updatedEntities,
                       final Iterable<SerializedEntity> removedEntities )
    {
        System.out.println( "New: " + newEntities );
        System.out.println( "Updated: " + updatedEntities );
        System.out.println( "Removed: " + removedEntities );

        try
        {
            final RepositoryConnection connection = repository.getConnection();
            final ValueFactory valueFactory = repository.getValueFactory();
            try
            {
                for( Map.Entry<SerializedEntity, SerializedState> entry : newEntities.entrySet() )
                {
                    final URI entityType = valueFactory.createURI(
                        "urn:" + entry.getKey().getCompositeType().getName()
                    );
                    final URI entityUri = valueFactory.createURI(
                        "urn:" + entry.getKey().getCompositeType().getName() + "/" + entry.getKey().getIdentity()
                    );
                    connection.add( entityUri, RDF.TYPE, entityType );
                    for( Map.Entry<String, Serializable> property : entry.getValue().getProperties().entrySet() )
                    {
                        final URI propType = valueFactory.createURI(
                            "urn:" + entry.getKey().getCompositeType().getName() + "/" + property.getKey()
                        );
                        final Literal propValue = valueFactory.createLiteral( property.getValue().toString() );
                        connection.add( entityUri, propType, propValue );
                    }
                    for( Map.Entry<String, SerializedEntity> assoc : entry.getValue().getAssociations().entrySet() )
                    {
                        final URI assocType = valueFactory.createURI(
                            "urn:" + entry.getKey().getCompositeType().getName() + "/" + assoc.getKey()
                        );
                        final URI assocUri = valueFactory.createURI(
                            "urn:" + assoc.getValue().getCompositeType().getName() + "/" + assoc.getValue().getIdentity()
                        );
                        final Literal assocValue = valueFactory.createLiteral( "x", assocUri);
                        connection.add( entityUri, assocType, assocValue );
                    }
                }
            }
            finally
            {
                if( connection != null )
                {
                    connection.commit();
                }
            }
        }
        catch( RepositoryException e )
        {
            e.printStackTrace();
        }
    }

    public void toRDF( final OutputStream outputStream )
    {
        RDFXMLWriter rdfWriter = new RDFXMLPrettyWriter( outputStream );
        try
        {
            final RepositoryConnection connection = repository.getConnection();
            try
            {
                connection.prepareGraphQuery( QueryLanguage.SERQL, "CONSTRUCT * FROM {x} p {y}" ).evaluate( rdfWriter );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                if( connection != null )
                {
                    connection.commit();
                }
            }
        }
        catch( RepositoryException e )
        {
            e.printStackTrace();
        }

    }
}
