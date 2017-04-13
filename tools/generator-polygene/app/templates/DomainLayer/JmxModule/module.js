module.exports = {

    write: function (p) {
        if( p.hasFeature('jmx')) {
            p.copyTemplate(p.ctx,
                'DomainLayer/JmxModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/domain/JmxModule.java');
        }
    }
};
