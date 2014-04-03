package com.scalr.ssh;

import com.scalr.ssh.logging.JTextAreaHandler;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHLauncherApplet extends JApplet {
    private final static Logger logger = Logger.getLogger(SSHLauncherApplet.class.getName());
    private final static int    paramLogLength = 20;

    public SSHLauncherApplet () {
    }

    public void init() {
        GridLayout layout = new GridLayout(1, 1);
        setLayout(layout);

        JTextArea textArea  = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);

        JScrollPane jScrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        getContentPane().add(jScrollPane);


        Handler textAreaHandler = new JTextAreaHandler(textArea);

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(textAreaHandler);


        String requestedLogLevel = getParameter(SSHLauncher.logLevelParam);
        Level logLevel;

        if (requestedLogLevel != null) {
            try {
                logLevel = Level.parse(requestedLogLevel);
            } catch (IllegalArgumentException e) {
                logLevel = Level.INFO;
                logger.warning(String.format("logLevel '%s' could not be parsed - defaulting to INFO", requestedLogLevel));
            }

            logger.info(String.format("Setting LogLevel to '%s'. Requested '%s'", logLevel.getName(),
                    requestedLogLevel));

            // Do not set this on the main logger. Too much output crashes the Java applet console (?!).
            Logger launcherLogger = Logger.getLogger("com.scalr.ssh");
            textAreaHandler.setLevel(logLevel);
            launcherLogger.setLevel(logLevel);
        }

        // Info
        logger.info("Initialized applet");

        String paramName;
        String paramValue;

        for (String[] paramDefinition : getParameterInfo()) {
            paramName = paramDefinition[0];
            paramValue = getParameter(paramName);
            if (paramValue == null) {
                paramValue = "";
            }
            if (paramValue.length() > paramLogLength) {
                paramValue = String.format("%s...", paramValue.substring(0, paramLogLength));
            }
            logger.config(String.format("Applet parameter '%s': '%s'", paramName, paramValue));
        }
    }


    public void start() {
        logger.info("Starting");

        SSHLauncher sshLauncher = new SSHLauncher(new AppletLauncherConfiguration(this));
        boolean hasSucceeded = sshLauncher.launch();

        if (!hasSucceeded){
            return;
        }

        String returnURL = getParameter(SSHLauncher.returnURLParam);
        if (returnURL == null) {
            return;
        }

        try {
            getAppletContext().showDocument(new URL(returnURL));
        } catch (MalformedURLException e) {
            logger.warning(String.format("Unable to exit: %s", e.toString()));
        }
    }

    public void stop() {
        logger.info("Exiting");
    }

    public void destroy() {
        logger.info("Unloading");
    }

    public String[][] getParameterInfo () {
        return SSHLauncher.getParameterInfo();
    }
}