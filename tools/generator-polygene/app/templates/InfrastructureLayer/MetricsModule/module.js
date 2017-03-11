module.exports = {

    write: function (p) {
        if (p.metrics !== "None") {
            p.copyTemplate(p.ctx,
                'InfrastructureLayer/MetricsModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/infrastructure/' + p.metrics + 'MetricsModule.java');
        }
    }
};
