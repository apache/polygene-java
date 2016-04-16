/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.tools.shell.create.project;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.zest.tools.shell.FileUtils;
import org.apache.zest.tools.shell.TestHelper;
import org.junit.Test;

import static org.apache.zest.tools.shell.FileUtils.removeDir;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RestAppProjectCreatorTest
{
    private RestAppProjectCreator underTest = new RestAppProjectCreator();

    @Test
    public void givenCorrectInputWhenCreatedProjectExpectCompleteProjectCreated()
        throws Exception
    {

        TestHelper.zetZestZome();
        File projectDir = new File( "ZestTest" );
        if( projectDir.exists() )
        {
            removeDir( projectDir );
        }
        Map<String, String> properties = new HashMap<>();
        properties.put( "zest.home", System.getProperty( "zest.home" ) );
        properties.put( "root.package", "org.apache.zest.test" );
        properties.put( "project.dir", "ZestTest" );
        properties.put( "project.name", "ZestTest" );
        properties.put( "template.dir", "etc/templates/restapp/files" );
        underTest.create( "ZestTest", projectDir, properties );

        assertThat( projectDir.exists(), equalTo( true ) );
        assertThat( new File( projectDir, "app/build.gradle" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "app/src" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "app/src/main/resources" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "app/src/main/java/org/apache/zest/test/app/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "app/src/main/java/org/apache/zest/test/app/ZestTest.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "app/src/main/webapp/WEB-INF/web.xml" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "app/src/test/java/org/apache/zest/test/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/config/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/config/ConfigModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/config/ConfigurationLayer.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/connectivity/ConnectivityLayer.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/connectivity/RestModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/connectivity/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/domain/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/domain/CrudModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/domain/DomainLayer.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/domain/OrderModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/domain/SecurityModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/infrastructure/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/infrastructure/FileConfigurationModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/infrastructure/IndexingModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/infrastructure/InfrastructureLayer.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/infrastructure/SerializationModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/infrastructure/StorageModule.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/java/org/apache/zest/test/bootstrap/ZestTestApplicationAssembler.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main/resources" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/test/java/org/apache/zest/test/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/build.gradle" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/main" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "bootstrap/src/test/java/org/apache/zest/test/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/build.gradle" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/resources" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/java/org/apache/zest/test/model/orders/Customer.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/java/org/apache/zest/test/model/orders/Order.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/java/org/apache/zest/test/model/orders/OrderItem.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/java/org/apache/zest/test/model/orders/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/java/org/apache/zest/test/model/security/HardcodedSecurityRepositoryMixin.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/java/org/apache/zest/test/model/security/SecurityRepository.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/main/java/org/apache/zest/test/model/security/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "model/src/test/java/org/apache/zest/test/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "rest/build.gradle" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "rest/src" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "rest/src/main/resources" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "rest/src/main/java/org/apache/zest/test/rest/security/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "rest/src/main/java/org/apache/zest/test/rest/security/SimpleEnroler.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "rest/src/main/java/org/apache/zest/test/rest/security/SimpleVerifier.java" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "rest/src/test/java/org/apache/zest/test/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "gradle/wrapper/gradle-wrapper.jar" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "gradle/wrapper/gradle-wrapper.properties" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "gradlew" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "gradlew.bat" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "build.gradle" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "settings.gradle" ).exists(), equalTo( true ) );
        if( FileUtils.removeDir( projectDir ) )
        {
            System.err.println( "Unable to remove file. Why???" );
        }
        assertThat( projectDir.exists(), equalTo( false ) );
    }
}
