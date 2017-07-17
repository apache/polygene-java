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
 */

module.exports = {

    write: function (p) {
        copyBuildFile(p, "app");
        copyBuildFile(p, "bootstrap");
        copyBuildFile(p, "model");
        if( p.applicationtype === 'Rest API'){
            copyBuildFile(p, "rest");
        }
        p.copyTemplate(p.ctx, 'buildtool/gradle-root.tmpl', 'build.gradle');
        p.copyTemplate(p.ctx, 'buildtool/settings.tmpl', 'settings.gradle');
        p.copyTemplate(p.ctx, 'buildtool/wrapper/gradlew.tmpl', 'gradlew');
        p.copyTemplate(p.ctx, 'buildtool/wrapper/gradlew-bat.tmpl', 'gradlew.bat');

        p.copyBinary(p.ctx, 'buildtool/wrapper/gradle-wrapper.jar_', 'gradle/wrapper/gradle-wrapper.jar');
        p.copyBinary(p.ctx, 'buildtool/wrapper/gradle-wrapper.properties_','gradle/wrapper/gradle-wrapper.properties');
    }
};

function copyBuildFile(p, subproject) {
    p.copyTemplate(p.ctx,
        'buildtool/gradle-' + subproject + '.tmpl',
        subproject + '/build.gradle');
}
