
package org.conjur.jenkins.conjursecrets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import hudson.util.Secret;

@RunWith(MockitoJUnitRunner.class)
public class ConjurSecretCredentialImplTest {

	@Test
	public void mockGetSecret() {
		// Create a mock of the conjurSecretCredentials Class
		ConjurSecretCredentialsImpl conjurSecretCredentials = mock(ConjurSecretCredentialsImpl.class);
		Secret secret = mock(Secret.class);
		when(conjurSecretCredentials.getSecret()).thenReturn(secret);
		Secret returnedSecret = conjurSecretCredentials.getSecret();
		// Verify that the method was called and the correct secret is returned
		verify(conjurSecretCredentials).getSecret();
		assertEquals(secret, returnedSecret);

	}

}