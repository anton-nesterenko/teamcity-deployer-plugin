package jetbrains.buildServer.deployer.agent.cargo;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.deployer.agent.SyncBuildProcessAdapter;
import jetbrains.buildServer.deployer.common.DeployerRunnerConstants;
import jetbrains.buildServer.util.StringUtil;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.SimpleLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Nikita.Skvortsov
 * date: 25.06.2014.
 */
public class CargoBuildProcessAdapter extends SyncBuildProcessAdapter {

    private final String myHost;
    private final String myPort;
    private final String myUsername;
    private final String myPassword;
    private final BuildRunnerContext myContext;
    private final String mySourcePath;
    private final String myContainerType;

    public CargoBuildProcessAdapter(@NotNull String target,
                                    @NotNull String username,
                                    @NotNull String password,
                                    @NotNull BuildRunnerContext context,
                                    @NotNull String sourcePath) {
        super(context.getBuild().getBuildLogger());
        myHost = getHost(target);
        myPort = getPort(target);
        myUsername = username;
        myPassword = password;
        myContext = context;
        mySourcePath = sourcePath;
        myContainerType = context.getRunnerParameters().get(DeployerRunnerConstants.PARAM_CONTAINER_TYPE);
    }

    private String getHost(@NotNull String target) {
        if (target.indexOf(':') > 0) {
            return target.substring(0, target.indexOf(':'));
        }
        return target;
    }

    private String getPort(@NotNull String target) {
        if (target.indexOf(':') > 0) {
            return target.substring(target.indexOf(':') + 1);
        }
        return "";
    }

    @Override
    protected void runProcess() throws RunBuildException {
        try {
            final ConfigurationFactory configFactory = new DefaultConfigurationFactory();
            final Configuration configuration = configFactory.createConfiguration(myContainerType, ContainerType.REMOTE, ConfigurationType.RUNTIME);

            configuration.setProperty(RemotePropertySet.USERNAME, myUsername);
            configuration.setProperty(RemotePropertySet.PASSWORD, myPassword);
            configuration.setProperty(GeneralPropertySet.HOSTNAME, myHost);
            if (!StringUtil.isEmpty(myPort)) {
                configuration.setProperty(ServletPropertySet.PORT, myPort);
            }


            final DefaultContainerFactory containerFactory = new DefaultContainerFactory();
            final Container container = containerFactory.createContainer(myContainerType, ContainerType.REMOTE, configuration);

            final DefaultDeployerFactory deployerFactory  = new DefaultDeployerFactory();
            final Deployer deployer = deployerFactory.createDeployer(container);

            final DefaultDeployableFactory deployableFactory = new DefaultDeployableFactory();
            final Deployable deployable = deployableFactory.createDeployable(container.getId(), getLocation(mySourcePath), DeployableType.WAR);
            myLogger.message("Deploying [" + mySourcePath + "] to ["
                    + configuration.getPropertyValue(GeneralPropertySet.HOSTNAME) + ":" + configuration.getPropertyValue(ServletPropertySet.PORT)
                    + "], container type [" + myContainerType +"]");


            final SimpleLogger simpleLogger = new SimpleLogger();
            simpleLogger.setLevel(LogLevel.DEBUG);
            deployer.setLogger(simpleLogger);
            deployer.redeploy(deployable);
        } catch (Exception e) {
            throw new RunBuildException(e);
        }
        myLogger.message("Deploy finished.");
    }

    private String getLocation(String mySourcePath) {
        return new File(myContext.getWorkingDirectory(), mySourcePath).getAbsolutePath();
    }
}
