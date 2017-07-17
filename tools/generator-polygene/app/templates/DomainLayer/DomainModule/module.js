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
        if(!p.modules) return;
        Object.keys(p.modules).forEach(function (moduleName, index) {
            copyPolygeneDomainModule(p, moduleName, p.modules[moduleName])
        });
    }

};

function copyFile(p, module, clazz) {
    p.copyTemplate(p.ctx,
        p.firstUpper(module) + 'Module/' + clazz + '.tmpl',
        'model/src/main/java/' + p.javaPackageDir + '/model/' + module + '/' + clazz + '.java');
}

function copyPolygeneDomainModule(p, moduleName, moduleDef) {
    p.current = moduleDef;
    p.current.name = moduleName;
    var clazz = p.firstUpper(moduleName) + "Module";
    p.copyTemplate(p.ctx,
        'DomainLayer/DomainModule/bootstrap.tmpl',
        'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/domain/' + clazz + '.java');
    copyComposites(p, moduleDef.cruds, "Crud");
    copyComposites(p, moduleDef.entities, "Entity");
    copyComposites(p, moduleDef.values, "Value");
    copyComposites(p, moduleDef.transients, "Transient");
    copyComposites(p, moduleDef.objects, "Object");
    copyComposites(p, moduleDef.services, "Service");
    copyComposites(p, moduleDef.plainTypes, "Plain");
    copyComposites(p, moduleDef.services, "Configuration");

    copyConfigurationPropertiesFile(p, moduleDef.services )
}

function copyComposites(p, composites, type) {
    for (var idx in composites) {
        if (composites.hasOwnProperty(idx)) {
            if( type === "Configuration"){
                p.current.clazz.name = p.configurationClassName(composites[idx].name);
                delete p.current.type;
                delete p.current.value;
                p.prepareConfigClazz(p.current, composites[idx]);
            } else {
                p.current.clazz = composites[idx];
                p.prepareClazz(p.current);
            }
            p.copyTemplate(p.ctx,
                'DomainLayer/DomainModule/' + type + '.tmpl',
                'model/src/main/java/' + p.javaPackageDir + '/model/' + p.current.name  + '/' + p.current.clazz.name + '.java');
        }
    }
}

function copyConfigurationPropertiesFile(p, composites) {
    for (var idx in composites) {
        if (composites.hasOwnProperty(idx)) {
            p.current.clazz = composites[idx];
            p.prepareClazz(p.current);
            var configurationFile = 'DomainLayer/DomainModule/config.properties.tmpl';
            var destFileName = p.current.clazz.name + '.properties';
            p.copyToConfig(p.ctx,configurationFile, destFileName);
        }
    }
}

