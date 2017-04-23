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
        if( p.hasFeature("security")) {

            p.copyTemplate(p.ctx,
                'DomainLayer/SecurityModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/domain/SecurityModule.java');

            copyFile(p, "CryptoConfiguration");
            copyFile(p, "CryptoException");
            copyFile(p, "CryptoService");
            copyFile(p, "EncryptedStringPropertyConcern");
            copyFile(p, "Group");
            copyFile(p, "RealmService");
            copyFile(p, "SecurityRepository");
            copyFile(p, "User");
            copyFile(p, "UserFactory");
        }
    }

};

function copyFile(p, clazz) {
    p.copyTemplate(p.ctx,
        'DomainLayer/SecurityModule/' + clazz + '.tmpl',
        'model/src/main/java/' + p.javaPackageDir + '/model/security/' + clazz + '.java');
}
