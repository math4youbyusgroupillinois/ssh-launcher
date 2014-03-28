package com.scalr.ssh.launcher;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import com.scalr.ssh.manager.UnixSSHManager;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class MacNativeSSHLauncher extends BaseSSHLauncher {
    public MacNativeSSHLauncher(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }
    private NSDictionary getBaseTerminalConfiguration () {
        return AccessController.doPrivileged(new PrivilegedAction<NSDictionary>() {
            final String[] pathBits = {FileSystemManager.getUserHome(), "Library", "Preferences",
                                       "com.apple.Terminal.plist"};

            @Override
            public NSDictionary run() {
                File configFile = new File(StringUtils.join(pathBits, File.separator));

                NSDictionary root;

                try {
                    root = (NSDictionary) PropertyListParser.parse(configFile);
                } catch (Exception e) {
                    //TODO: Fix.
                    e.printStackTrace();
                    return new NSDictionary();  // Default configuration
                }

                String defaultConfiguration = root.objectForKey("Default Window Settings").toString();
                // TODO -> This is unsafe.
                NSDictionary configurations = (NSDictionary) root.objectForKey("Window Settings");

                NSObject ret = configurations.objectForKey(defaultConfiguration);

                if (ret == null) {
                    return new NSDictionary();
                }

                return (NSDictionary) ret;

            }
        });
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        SSHManagerInterface sshManager = new UnixSSHManager(sshConfiguration);
        sshManager.setUpSSHEnvironment();

        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();
        String   sshCommandLine = StringUtils.join(sshCommandLineBits, " ");

        final NSDictionary root = getBaseTerminalConfiguration();
        root.put("CommandString", sshCommandLine);
        root.put("RunCommandAsShell", true);
        root.put("name", "Scalr SSH");
        root.put("shellExitAction", 0);
        root.put("type", "Window Settings");

        File commandFile;

        try {
            commandFile = FileSystemManager.getTemporaryFile("ssh-command", ".terminal");
        } catch (IOException e) {
            throw new LauncherException("Unable to provision a temporary file for Terminal command.");
        }

        try {
            PropertyListParser.saveAsXML(root, commandFile);
        } catch (IOException e) {
            throw new LauncherException("Unable to write Terminal command to temporary file.");
        }

        String canonicalPath;
        try {
            canonicalPath = commandFile.getCanonicalPath();
        } catch (IOException e) {
            throw new LauncherException("Terminal command file has no canonical path");
        }

        ProcessBuilder pb = new ProcessBuilder().command("/usr/bin/xattr", "-d", "com.apple.quarantine", canonicalPath);
        try {
            Process p = pb.start();
            Integer ret = p.waitFor();

            if (ret != 0) {
                throw new LauncherException(String.format("Unable to remove Terminal command file from quarantine: %s", ret));
            }
        } catch (IOException e) {
            throw new LauncherException("xattr not found.");
        } catch (InterruptedException e) {
            throw new LauncherException("Interrupted when removing Terminal command file from quarantine.");
        }

        return new String[] {"/usr/bin/open", canonicalPath};
    }
}