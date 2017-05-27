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
        p.copyTemplate(p.ctx,
            'InfrastructureLayer/StorageModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/infrastructure/' + p.entitystore + 'StorageModule.java');

        var configurationFile = 'InfrastructureLayer/StorageModule/storage/es-' + p.entitystore.toLowerCase() + '.properties';
        var confFile = require(configurationFile);
        if (confFile.existsSync(path)) {
            p.copyTemplate(p.ctx,
                configurationFile,
                'app/src/main/resources/config/');
        }

        var datasourceFile = 'InfrastructureLayer/StorageModule/storage/ds-' + p.entitystore.toLowerCase() + '.properties';
        var dsFile = require(datasourceFile);
        if (dsFile.existsSync(path)) {
            p.copyTemplate(p.ctx,
                'InfrastructureLayer/StorageModule/storage/es-sql.properties',
                'app/src/main/resources/config/es-' + p.entitystore.toLowerCase());
            p.copyTemplate(p.ctx,
                configurationFile,
                'app/src/main/resources/config/');
        }
    }
};
