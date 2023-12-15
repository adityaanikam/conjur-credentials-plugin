
package org.conjur.jenkins.conjursecrets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;


import hudson.model.ModelObject;

@RunWith(MockitoJUnitRunner.class)
public class ConjurSecretCredentiTest {

	@Test
	public void mockCredentialFromContextIfNeeded() {
		try (MockedStatic<ConjurSecretCredentials> mockedStaticConjurSecretCredentials = mockStatic(
				ConjurSecretCredentials.class)) {
			final ConjurSecretCredentials conjurSecretCredentials = mock(ConjurSecretCredentials.class);
			mock(ModelObject.class);
			final ModelObject context = mock(ModelObject.class);
			String credentialID = "Id74";
			mockedStaticConjurSecretCredentials.when(() -> ConjurSecretCredentials
					.credentialFromContextIfNeeded(conjurSecretCredentials, credentialID, context))
					.thenReturn(conjurSecretCredentials);

		}

	}

	@Test
	public void mockCredentialWithId() {
		try (MockedStatic<ConjurSecretCredentials> mockedStaticConjurSecretCredentials = mockStatic(
				ConjurSecretCredentials.class)) {
			final ConjurSecretCredentials conjurSecretCredentialsId = mock(ConjurSecretCredentials.class);
			mockStatic(ModelObject.class);
			final ModelObject context = mock(ModelObject.class);
			String credentialID = "Id412";
			mockedStaticConjurSecretCredentials
					.when(() -> ConjurSecretCredentials.credentialWithID(credentialID, context))
					.thenReturn(conjurSecretCredentialsId);

		}
	}
}