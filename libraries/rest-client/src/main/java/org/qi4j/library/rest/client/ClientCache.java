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

package org.qi4j.library.rest.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Tag;

/**
 * Cache for the ContextResourceClient. This is primarily used to keep track of ETags and lastmodified timestamps for now.
 */
public class ClientCache
{
   Map<String, CacheInfo> identityToTimestamp = new HashMap<String, CacheInfo>( );
   Map<String, String> pathToIdentity = new HashMap<String, String>( );

   public void updateCache( Response response)
   {
      if (response.getRequest().getMethod().equals( Method.DELETE ))
      {
         String path = getIdentityPath( response.getRequest().getResourceRef() );
         String id = pathToIdentity.get( path );
         if (id != null)
         {
            // Clear anything related to this id from cache
            identityToTimestamp.remove( id );
            Iterator<Map.Entry<String, String>> paths = pathToIdentity.entrySet().iterator();
            while (paths.hasNext())
            {
               Map.Entry<String, String> entry = paths.next();
               if (entry.getValue().equals( id ))
                  paths.remove();
            }
         }
      } else if (response.getRequest().getMethod().equals( Method.PUT ) || response.getRequest().getMethod().equals( Method.POST ))
      {
         Tag tag = response.getEntity().getTag();
         if (tag != null)
         {
            Reference ref = response.getRequest().getResourceRef().clone();

            CacheInfo value = new CacheInfo( response.getEntity().getModificationDate(), tag, ref );
            identityToTimestamp.put( value.getEntity(), value );

            String path = getIdentityPath( ref );

            pathToIdentity.put( path, value.getEntity() );

//            LoggerFactory.getLogger( ClientCache.class ).info( "Update:"+value.getEntity()+" ("+ref.toString()+") -> "+value.getLastModified() );
         }
      } else
      {
          // TODO GET caching goes here
      }
   }

   public void updateQueryConditions( Request request)
   {
      String identity = pathToIdentity.get( getIdentityPath( request.getResourceRef() ));
      if (identity != null)
      {
         CacheInfo cacheInfo = identityToTimestamp.get( identity );
         if (cacheInfo != null)
         {
//            LoggerFactory.getLogger( ClientCache.class ).info( "Send:  "+cacheInfo.getEntity()+" ("+request.getMethod().getName()+":"+request.getResourceRef()+") -> "+cacheInfo.getLastModified() );
            request.getConditions().setModifiedSince( cacheInfo.getLastModified() );
         }
      }
   }

   public void updateCommandConditions( Request request)
   {
      String identity = pathToIdentity.get( getIdentityPath( request.getResourceRef() ));
      if (identity != null)
      {
         CacheInfo cacheInfo = identityToTimestamp.get( identity );
         if (cacheInfo != null)
         {
//            LoggerFactory.getLogger( ClientCache.class ).info( "Send:  "+cacheInfo.getEntity()+" ("+request.getMethod().getName()+":"+request.getResourceRef()+") -> "+cacheInfo.getLastModified() );
            request.getConditions().setUnmodifiedSince( cacheInfo.getLastModified() );
         }
      }
   }

   private String getIdentityPath( Reference ref )
   {
      String path = ref.getPath();
      if (!path.endsWith( "/" ))
         path = path.substring( 0, path.lastIndexOf('/' )+1);
      return path;
   }

   static class CacheInfo
   {
      private Reference ref;
      private Date lastModified;
      private Tag tag;
      private String entity;

      CacheInfo( Date lastModified, Tag tag, Reference ref )
      {
         this.lastModified = lastModified;
         this.tag = tag;
         this.ref = ref;
         entity = tag.getName().split( "/" )[0];
      }

      public Reference getRef()
      {
         return ref;
      }

      public Date getLastModified()
      {
         return lastModified;
      }

      public Tag getTag()
      {
         return tag;
      }

      public String getEntity()
      {
         return entity;
      }
   }
}
