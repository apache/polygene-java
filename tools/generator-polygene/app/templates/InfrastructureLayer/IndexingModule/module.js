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
            'InfrastructureLayer/IndexingModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/infrastructure/' + p.indexing + 'IndexingModule.java');
        if (p.indexing === 'SQL') {
            p.copyToConfig(
                p.ctx,
                'InfrastructureLayer/IndexingModule/indexing/ds-index-postgresql.properties',
                'ds-index-postgresql.properties'
            );
        }
        if (p.indexing === 'Solr') {
            p.copyToConfig(
                p.ctx,
                'InfrastructureLayer/IndexingModule/indexing/solrconfig.xml',
                'solrconfig.xml'
            );
            p.copyToConfig(
                p.ctx,
                'InfrastructureLayer/IndexingModule/indexing/solr-schema.xml',
                'schema.xml'
            );
        }
    }
};
