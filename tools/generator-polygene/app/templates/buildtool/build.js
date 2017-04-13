module.exports = {

    write: function (p) {
        copyBuildFile(p, "app");
        copyBuildFile(p, "bootstrap");
        copyBuildFile(p, "model");
        if( p.applicationtype === 'Rest API'){
            copyBuildFile(p, "rest");
        }
        p.copyTemplate(p.ctx, 'buildtool/gradle-root.tmpl', 'build.gradle');
        p.copyTemplate(p.ctx, 'buildtool/settings.tmpl', 'settings.gradle');
        p.copyTemplate(p.ctx, 'buildtool/gradlew.tmpl', 'gradlew');
        p.copyTemplate(p.ctx, 'buildtool/gradlew-bat.tmpl', 'gradlew.bat');

        p.ctx.fs.copy(p.ctx.templatePath('buildtool/gradle-wrapper.jar_'), p.ctx.destinationPath('gradle/wrapper/gradle-wrapper.jar'));
        p.ctx.fs.copy(p.ctx.templatePath('buildtool/gradle-wrapper.properties_'), p.ctx.destinationPath('gradle/wrapper/gradle-wrapper.properties'));
    }
};

function copyBuildFile(p, subproject) {
    p.copyTemplate(p.ctx,
        'buildtool/gradle-' + subproject + '.tmpl',
        subproject + '/build.gradle');
}
