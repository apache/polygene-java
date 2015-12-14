/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.sample.forum.data.entity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;

/**
 * TODO
 */
@Mixins( User.Mixin.class )
public interface User
    extends EntityComposite
{
    @Immutable
    public Property<String> name();

    public Property<String> realName();

    public Property<String> email();

    public Property<String> password();

    public String hashPassword( String password );

    public boolean isCorrectPassword( String password );

    abstract class Mixin
        implements User
    {
        @Override
        public String hashPassword( String password )
        {
            try
            {
                MessageDigest md = MessageDigest.getInstance( "SHA" );
                md.update( password.getBytes( "UTF-8" ) );
                byte raw[] = md.digest();

                return Base64.getEncoder().encodeToString( raw );
            }
            catch( NoSuchAlgorithmException e )
            {
                throw new IllegalStateException( "No SHA algorithm found", e );
            }
            catch( UnsupportedEncodingException e )
            {
                throw new IllegalStateException( e.getMessage(), e );
            }
        }

        @Override
        public boolean isCorrectPassword( String password )
        {
            return password().get().equals( hashPassword( password ) );
        }
    }
}
