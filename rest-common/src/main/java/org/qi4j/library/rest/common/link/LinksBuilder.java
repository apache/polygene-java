/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.common.link;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.qi4j.api.common.Optional;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * Builder for making it easier to create LinksValue/LinkValue
 */
public class LinksBuilder<T extends LinksBuilder>
{
    protected ValueBuilder<? extends Links> linksBuilder;
    protected ValueBuilder<Link> linkBuilder;
    protected ValueBuilderFactory vbf;

    private String path;
    private String rel;
    private String classes;
    private String command;

    public LinksBuilder( ValueBuilderFactory vbf )
    {
        this.vbf = vbf;
        linksBuilder = vbf.newValueBuilder( Links.class );
        linkBuilder = vbf.newValueBuilder( Link.class );
    }

    public T path( @Optional String subPath )
    {
        path = subPath;

        return (T) this;
    }

    public T rel( String rel )
    {
        this.rel = rel;

        return (T) this;
    }

    public T classes( String classes )
    {
        this.classes = classes;

        return (T) this;
    }

    public T command( String commandName )
    {
        this.command = commandName;
        this.rel = commandName;
        return (T) this;
    }

    public T addLink( Link link )
    {
        linksBuilder.prototype().links().get().add( link );

        linkBuilder = vbf.newValueBuilderWithPrototype( link );

        return (T) this;
    }

    public T addLink( String description, String id )
    {
        try
        {
            linkBuilder.prototype().text().set( description );
            linkBuilder.prototype().id().set( id );
            if( command != null )
            {
                linkBuilder.prototype().href().set( command + "?entity=" + id );
            }
            else
            {
                linkBuilder.prototype()
                    .href()
                    .set( ( path == null ? "" : path + "/" ) + URLEncoder.encode( id, "UTF-8" ) + "/" );
            }
            linkBuilder.prototype().rel().set( rel );
            linkBuilder.prototype().classes().set( classes );

            addLink( linkBuilder.newInstance() );

            return (T) this;
        }
        catch( UnsupportedEncodingException e )
        {
            e.printStackTrace();
            return (T) this;
        }
    }

    public T addLink( String description, String id, String rel, String href, String classes )
    {
        linkBuilder.prototype().text().set( description );
        linkBuilder.prototype().id().set( id );
        linkBuilder.prototype().rel().set( rel );
        linkBuilder.prototype().href().set( href );
        linkBuilder.prototype().classes().set( classes );

        addLink( linkBuilder.newInstance() );

        return (T) this;
    }

    public Links newLinks()
    {
        return linksBuilder.newInstance();
    }
}