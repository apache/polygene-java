
module.exports = {

    write: function (p) {
        if (p.caching !== "None") {

            p.copyTemplate(p.ctx,
                'InfrastructureLayer/CachingModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/infrastructure/' + p.caching + 'CachingModule.java');
        }
    }
};
