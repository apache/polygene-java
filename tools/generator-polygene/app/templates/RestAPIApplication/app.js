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
        copyLayer(p, "Configuration");
        copyLayer(p, "Infrastructure");
        copyLayer(p, "Domain");
        copyLayer(p, "Connectivity");

        p.copyTemplate(p.ctx,
            'RestAPIApplication/application.java.tmpl',
            'rest/src/main/java/' + p.javaPackageDir + '/rest/' + p.name + 'RestApplication.java');

        p.copyTemplate(p.ctx,
            'RestAPIApplication/Launcher.java.tmpl',
            'app/src/main/java/' + p.javaPackageDir + '/app/' + p.name + 'Launcher.java');

        if (p.hasFeature('security')) {
            p.copyToConfig(p.ctx, 'RestAPIApplication/web-shiro.ini.tmpl', 'web-shiro.ini');
            p.copyBinary(p.ctx, 'RestAPIApplication/dev-keystore.p12', 'app/src/dist/config/development/security/keystore-' + p.name.toLowerCase() + '.p12');
            p.copyBinary(p.ctx, 'RestAPIApplication/dev-keystore.p12', 'app/src/test/resources/security/keystore-' + p.name.toLowerCase() + '.p12');
        }

        p.copyTemplate(p.ctx,
            'RestAPIApplication/bootstrap-test.tmpl',
            'app/src/test/java/' + p.javaPackageDir + '/app/BootstrapTest.java');

        p.copyTemplate(p.ctx,
            'RestAPIApplication/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/' + p.name + 'ApplicationAssembler.java');

        p.copyTemplate(p.ctx,
            'RestAPIApplication/SETUP.tmpl',
            'SETUP.txt');

        p.copyTemplate(p.ctx,
            'RestAPIApplication/customUnixStartScript.txt.tmpl',
            'app/src/scripts/customUnixStartScript.txt');

        p.copyTemplate(p.ctx,
            'RestAPIApplication/customWindowsStartScript.txt.tmpl',
            'app/src/scripts/customWindowsStartScript.txt');
    }
};

function copyLayer(p, layerName) {
    var layer = require(__dirname + '/../' + layerName + 'Layer/layer.js');
    layer.write(p);
}
