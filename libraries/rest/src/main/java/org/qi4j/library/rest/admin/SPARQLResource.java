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

package org.qi4j.library.rest.admin;

import info.aduna.xml.XMLWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriterFactory;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.api.injection.scope.Service;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import static org.openrdf.http.protocol.Protocol.*;

/**
 * JAVADOC
 */
public class SPARQLResource
    extends ServerResource
{
    @Service
    Repository repository;

    public SPARQLResource()
    {
        getVariants().addAll( Arrays.asList(
            new Variant( MediaType.TEXT_HTML ),
            new Variant( MediaType.APPLICATION_RDF_XML ),
            new Variant( RestApplication.APPLICATION_SPARQL_JSON ) ) );
        setNegotiated( true );
    }

    @Override
    protected void doInit()
        throws ResourceException
    {
        super.doInit();
    }

    @Override
    public Representation get( final Variant variant )
        throws ResourceException
    {
        try
        {
            // TODO There's probably a helper somewhere that can do this more nicely
            if( getRequest().getOriginalRef().getLastSegment().equals( "sparqlhtml.xsl" ) )
            {
                InputStream resourceAsStream = getClass().getResourceAsStream( "sparqlhtml.xsl" );
                return new InputRepresentation( resourceAsStream, MediaType.TEXT_XML );
            }

            Form form;
            if( getRequest().getMethod().equals( Method.POST ) )
            {
                form = new Form(getRequest().getEntity());
            }
            else
            {
                form = getRequest().getResourceRef().getQueryAsForm();
            }

            final RepositoryConnection conn = repository.getConnection();

            String queryStr = form.getFirstValue( "query" );

            if( queryStr == null )
            {
                InputStream resourceAsStream = getClass().getResourceAsStream( "sparqlform.html" );
                return new InputRepresentation( resourceAsStream, MediaType.TEXT_HTML );
            }

            Query query = getQuery( repository, conn, queryStr );

            if( query instanceof TupleQuery )
            {
                TupleQuery tQuery = (TupleQuery) query;

                final TupleQueryResult queryResult = tQuery.evaluate();

                if( variant.getMediaType().equals( MediaType.TEXT_HTML ) )
                {
                    return new OutputRepresentation( MediaType.TEXT_XML )
                    {
                        @Override
                        public void write( OutputStream outputStream )
                            throws IOException
                        {
                            try
                            {
                                PrintWriter out = new PrintWriter( outputStream );
                                out.println( "<?xml version='1.0' encoding='UTF-8'?>" );
                                out.println( "<?xml-stylesheet type=\"text/xsl\" href=\"query/sparqlhtml.xsl\"?>" );
                                out.flush();
                                TupleQueryResultWriter qrWriter = new SPARQLResultsXMLWriter( new XMLWriter( outputStream )
                                {
                                    @Override
                                    public void startDocument()
                                        throws IOException
                                    {
                                        // Ignore
                                    }
                                } );
                                QueryResultUtil.report( queryResult, qrWriter );
                            }
                            catch( Exception e )
                            {
                                throw new IOException( e );
                            }
                            finally
                            {
                                try
                                {
                                    conn.close();
                                }
                                catch( RepositoryException e )
                                {
                                    // Ignore
                                }
                            }
                        }
                    };
                }
                else if( variant.getMediaType().equals( MediaType.APPLICATION_RDF_XML ) )
                {
                    return new OutputRepresentation( MediaType.APPLICATION_XML )
                    {
                        @Override
                        public void write( OutputStream outputStream )
                            throws IOException
                        {
                            try
                            {
                                TupleQueryResultWriter qrWriter = new SPARQLResultsXMLWriter( new XMLWriter( outputStream ) );
                                QueryResultUtil.report( queryResult, qrWriter );
                            }
                            catch( Exception e )
                            {
                                throw new IOException( e );
                            }
                            finally
                            {
                                try
                                {
                                    conn.close();
                                }
                                catch( RepositoryException e )
                                {
                                    // Ignore
                                }
                            }
                        }
                    };
                }
                else if( variant.getMediaType().equals( RestApplication.APPLICATION_SPARQL_JSON ) )
                {
                    return new OutputRepresentation( RestApplication.APPLICATION_SPARQL_JSON )
                    {
                        @Override
                        public void write( OutputStream outputStream )
                            throws IOException
                        {
                            try
                            {
                                TupleQueryResultWriter qrWriter = new SPARQLResultsJSONWriterFactory().getWriter( outputStream );
                                QueryResultUtil.report( queryResult, qrWriter );
                            }
                            catch( Exception e )
                            {
                                throw new IOException( e );
                            }
                            finally
                            {
                                try
                                {
                                    conn.close();
                                }
                                catch( RepositoryException e )
                                {
                                    // Ignore
                                }
                            }
                        }
                    };
                }
            }
            else if( query instanceof GraphQuery )
            {
                GraphQuery gQuery = (GraphQuery) query;

                /*
                                queryResult = gQuery.evaluate();
                                registry = RDFWriterRegistry.getInstance();
                                view = GraphQueryResultView.getInstance();
                */
                conn.close();
            }
            else if( query instanceof BooleanQuery )
            {
                BooleanQuery bQuery = (BooleanQuery) query;

                /*
                                queryResult = bQuery.evaluate();
                                registry = BooleanQueryResultWriterRegistry.getInstance();
                                view = BooleanQueryResultView.getInstance();
                */
                conn.close();
            }
            else
            {
                conn.close();
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported query type: "
                                                                              + query.getClass().getName() );
            }
        }
        catch( RepositoryException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
        catch( QueryEvaluationException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }

        return null;
    }

    private Query getQuery( Repository repository, RepositoryConnection repositoryCon, String queryStr
    )
        throws ResourceException
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();

        Query result;

// default query language is SPARQL
        QueryLanguage queryLn = QueryLanguage.SPARQL;

// determine if inferred triples should be included in query evaluation
        boolean includeInferred = true;

// build a dataset, if specified
        String[] defaultGraphURIs = form.getValuesArray( DEFAULT_GRAPH_PARAM_NAME );
        String[] namedGraphURIs = form.getValuesArray( NAMED_GRAPH_PARAM_NAME );

        DatasetImpl dataset = null;
        if( defaultGraphURIs.length > 0 || namedGraphURIs.length > 0 )
        {
            dataset = new DatasetImpl();

            if( defaultGraphURIs.length > 0 )
            {
                for( String defaultGraphURI : defaultGraphURIs )
                {
                    try
                    {
                        URI uri = repository.getValueFactory().createURI( defaultGraphURI );
                        dataset.addDefaultGraph( uri );
                    }
                    catch( IllegalArgumentException e )
                    {
                        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Illegal URI for default graph: "
                                                                                      + defaultGraphURI );
                    }
                }
            }

            if( namedGraphURIs.length > 0 )
            {
                for( String namedGraphURI : namedGraphURIs )
                {
                    try
                    {
                        URI uri = repository.getValueFactory().createURI( namedGraphURI );
                        dataset.addNamedGraph( uri );
                    }
                    catch( IllegalArgumentException e )
                    {
                        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Illegal URI for named graph: "
                                                                                      + namedGraphURI );
                    }
                }
            }
        }

        try
        {
            result = repositoryCon.prepareQuery( queryLn, queryStr );
            result.setIncludeInferred( includeInferred );

            if( dataset != null )
            {
                result.setDataset( dataset );
            }

// determine if any variable bindings have been set on this query.
            @SuppressWarnings( "unchecked" )
            Enumeration<String> parameterNames = Collections.enumeration( form.getValuesMap().keySet() );

            while( parameterNames.hasMoreElements() )
            {
                String parameterName = parameterNames.nextElement();

                if( parameterName.startsWith( BINDING_PREFIX ) && parameterName.length() > BINDING_PREFIX.length() )
                {
                    String bindingName = parameterName.substring( BINDING_PREFIX.length() );
                    Value bindingValue = parseValueParam( repository, form, parameterName );
                    result.setBinding( bindingName, bindingValue );
                }
            }
        }
        catch( UnsupportedQueryLanguageException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
        }
        catch( MalformedQueryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
        }
        catch( RepositoryException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }

        return result;
    }

    private Value parseValueParam( Repository repository, Form form, String parameterName )
        throws ResourceException
    {
        String paramValue = form.getFirstValue( parameterName );
        try
        {
            return Protocol.decodeValue( paramValue, repository.getValueFactory() );
        }
        catch( IllegalArgumentException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid value for parameter '" + parameterName + "': "
                                                                          + paramValue );
        }
    }
}
