
module.exports = {

    write: function (p) {
        if (p.hasFeature("rest api")) {
            p.copyTemplate(p.ctx,
                'ConnectivityLayer/RestApiModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/connectivity/RestApiModule.java');
            if (p.hasFeature("security")) {
                copyFile(p, "DefaultEnroler");
                copyFile(p, "DefaultVerifier");
            }
            else {
                copyFile(p, "NullEnroler");
                copyFile(p, "NullVerifier");
            }
        }
    }
};

function copyFile(p, clazz) {
    p.copyTemplate(p.ctx,
        'ConnectivityLayer/RestApiModule/' + clazz + '.tmpl',
        'rest/src/main/java/' + p.javaPackageDir + '/rest/security/' + clazz + '.java');
}
