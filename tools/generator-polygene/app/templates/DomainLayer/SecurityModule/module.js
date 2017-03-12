module.exports = {

    write: function (p) {
        if( p.hasFeature("security")) {

            p.copyTemplate(p.ctx,
                'DomainLayer/SecurityModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/domain/SecurityModule.java');

            copyFile(p, "CryptoConfiguration");
            copyFile(p, "CryptoException");
            copyFile(p, "CryptoService");
            copyFile(p, "EncryptedStringPropertyConcern");
            copyFile(p, "Group");
            copyFile(p, "RealmService");
            copyFile(p, "SecurityRepository");
            copyFile(p, "User");
            copyFile(p, "UserFactory");
        }
    }

};

function copyFile(p, clazz) {
    p.copyTemplate(p.ctx,
        'DomainLayer/SecurityModule/' + clazz + '.tmpl',
        'model/src/main/java/' + p.javaPackageDir + '/model/security/' + clazz + '.java');
}
