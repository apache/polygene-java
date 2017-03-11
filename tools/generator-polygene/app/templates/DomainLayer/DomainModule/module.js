module.exports = {

    write: function (p) {

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
}

function copyComposites(p, composites, type) {
    for (var idx in composites) {
        if (composites.hasOwnProperty(idx)) {
            p.current.clazz = composites[idx];
            p.copyTemplate(p.ctx,
                'DomainLayer/DomainModule/' + type + '.tmpl',
                'model/src/main/java/' + p.javaPackageDir + '/model/' + p.current.name  + '/' + p.current.clazz.name + '.java');
        }
    }
}