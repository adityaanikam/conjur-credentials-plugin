
package org.conjur.jenkins.credentials;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

@RunWith(MockitoJUnitRunner.class)

public class ConjurCredentialProviderTest {

	@Mock
	public ConjurCredentialProvider provider;

	@Test
	public void getStoreTest() {
		ConjurCredentialStore store = null;
		when(provider.getStore(any())).thenReturn(store);
		assertFalse(provider.getStore(any()) instanceof ConjurCredentialStore);
	}

	@Test(expected = RuntimeException.class)
	public void getCredentialsTest() throws Exception {

		ConjurCredentialProvider classUnderTest = Mockito.spy(new ConjurCredentialProvider());
		// Stub the private method using reflection
		Mockito.doReturn(Collections.emptyList()).when(classUnderTest.getClass())
				.getDeclaredMethod("getCredentialsFromSupplier", any(), any(), any());

	}

}