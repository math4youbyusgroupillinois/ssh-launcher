package com.scalr.ssh.provider.windows;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.SSHController;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.controller.PuTTYController;
import com.scalr.ssh.provider.base.BaseSSHProvider;

public class WindowsPuTTYProvider extends BaseSSHProvider {
    public WindowsPuTTYProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        SSHController sshController = new PuTTYController(sshConfiguration);
        sshController.setupEnvironment();
        return sshController.getSSHCommandLineBits();
    }
}
