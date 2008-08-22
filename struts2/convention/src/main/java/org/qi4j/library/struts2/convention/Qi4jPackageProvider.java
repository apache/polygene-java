package org.qi4j.library.struts2.convention;

import org.apache.struts2.convention.ActionConfigBuilder;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.inject.Inject;

public class Qi4jPackageProvider implements PackageProvider {

    private ActionConfigBuilder actionConfigBuilder;

    @Inject("qi4j")
    public Qi4jPackageProvider(ActionConfigBuilder actionConfigBuilder) {
        this.actionConfigBuilder = actionConfigBuilder;
    }

    public void init(Configuration configuration) throws ConfigurationException {
    }

    public void loadPackages() throws ConfigurationException {
        actionConfigBuilder.buildActionConfigs();
    }

    public boolean needsReload() {
        return false;
    }

}
