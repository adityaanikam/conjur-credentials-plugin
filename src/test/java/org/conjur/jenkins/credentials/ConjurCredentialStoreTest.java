
package org.conjur.jenkins.credentials;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;

import org.conjur.jenkins.conjursecrets.ConjurSecretCredentialsImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;

@RunWith(MockitoJUnitRunner.class)

public class ConjurCredentialStoreTest {

	@Test
	public void mockAddCredential() throws IOException {

		try (MockedStatic<ConjurCredentialStore> conjurCredentialStoreMockedStatic = mockStatic(
				ConjurCredentialStore.class)) {
			ConjurCredentialStore conjurCredentialStore = mock(ConjurCredentialStore.class);

			ConjurSecretCredentialsImpl conjurSecretCredentialsImplAdd = new ConjurSecretCredentialsImpl(
					CredentialsScope.GLOBAL, "DB_SECRET", "db/db_password", "Conjur Secret");

			conjurCredentialStoreMockedStatic
					.when(() -> conjurCredentialStore.addCredentials(Domain.global(), conjurSecretCredentialsImplAdd))
					.thenReturn(true);

			assertTrue(conjurCredentialStore.addCredentials(Domain.global(), conjurSecretCredentialsImplAdd));

		}

	}

	@Test
	public void mockRemoveCredential() throws IOException {
		mock(ConjurCredentialStore.class);
		ConjurCredentialStore conjurCredentialStore1Remove = mock(ConjurCredentialStore.class);

		ConjurSecretCredentialsImpl conjurSecretCredentialsImplRemove = new ConjurSecretCredentialsImpl(
				CredentialsScope.GLOBAL, "DB_SECRET", "db/db_password", "Conjur Secret");
		conjurCredentialStore1Remove.removeCredentials(Domain.global(), conjurSecretCredentialsImplRemove);

	}

	@Test
	public void mockUpdateCredential() throws IOException {
		mock(ConjurCredentialStore.class);
		ConjurCredentialStore conjurCredentialStoreUpdate = mock(ConjurCredentialStore.class);

		ConjurSecretCredentialsImpl conjurSecretCredentialsImplUpdate = new ConjurSecretCredentialsImpl(
				CredentialsScope.GLOBAL, "DB_SECRET", "db/db_password", "Conjur Secret");
		ConjurSecretCredentialsImpl conjurSecretCredentialsImplUpdate2 = new ConjurSecretCredentialsImpl(
				CredentialsScope.GLOBAL, "DB_SECRET1", "db/db_password", "Conjur Secret");
		conjurCredentialStoreUpdate.updateCredentials(Domain.global(), conjurSecretCredentialsImplUpdate,
				conjurSecretCredentialsImplUpdate2);

	}

}