
module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'UserInterfaceLayer/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/ui/UserInterfaceLayer.java');
        p.copyModules(__dirname );
    }
};
