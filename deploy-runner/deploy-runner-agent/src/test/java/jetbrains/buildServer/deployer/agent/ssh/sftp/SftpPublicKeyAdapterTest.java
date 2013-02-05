package jetbrains.buildServer.deployer.agent.ssh.sftp;

import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.deployer.agent.ssh.BaseSSHTransferTest;

import java.io.File;

/**
 * Created by Nikita.Skvortsov
 * Date: 1/21/13, 6:39 PM
 */
public class SftpPublicKeyAdapterTest extends BaseSSHTransferTest {
    @Override
    protected BuildProcess getProcess(String targetBasePath) {
        return new SftpBuildProcessAdapter(myPrivateKey, myUsername, "passphrase", targetBasePath,  PORT_NUM, myContext, myArtifactsCollections);
    }
}