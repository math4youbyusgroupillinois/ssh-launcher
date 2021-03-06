package provider;

import com.scalr.ssh.provider.SSHProvider;
import com.scalr.ssh.provider.manager.SSHProviderManager;
import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SSHProviderManagerTestCase {
    private SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");

    @Test
    public void testPreferredProvider () throws InvalidEnvironmentException {
        SSHProviderManager sshProviderManager = new SSHProviderManager("mac os x");
        ArrayList<SSHProvider> sshProviders;

        sshProviders = sshProviderManager.getOrderedSSHProviders(sshConfiguration, null);
        assertEquals(3, sshProviders.size());
        assertEquals("com.scalr.ssh.provider.mac.MacAppleScriptSSHProvider", sshProviders.get(0).getClass().getCanonicalName());

        sshProviders = sshProviderManager.getOrderedSSHProviders(sshConfiguration, "com.scalr.ssh.provider.mac.MacNativeSSHProvider");
        assertEquals(3, sshProviders.size());
        assertEquals("com.scalr.ssh.provider.mac.MacNativeSSHProvider", sshProviders.get(0).getClass().getCanonicalName());

    }
}
