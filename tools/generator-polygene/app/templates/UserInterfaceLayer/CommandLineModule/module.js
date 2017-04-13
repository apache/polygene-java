
module.exports = {

    write: function (p) {
        if (p.applicationtype === 'Command Line') {
            // NOT SUPPORTED YET!!!
            p.copyTemplate(p.ctx,
                'UserInterfaceLayer/CommandLineModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/ui/CommandLineModule.java');
        }
    }
};
