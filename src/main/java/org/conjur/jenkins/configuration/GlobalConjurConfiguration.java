package org.conjur.jenkins.configuration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class GlobalConjurConfiguration extends GlobalConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private ConjurConfiguration conjurConfiguration;
	private Boolean enableJWKS = false;
	private String authWebServiceId = "";
	private String jwtAudience = "";
	private long keyLifetimeInMinutes = 60;
	private long tokenDurarionInSeconds = 120;
	private Boolean enableContextAwareCredentialStore = false;
	private String identityFormatFieldsFromToken = "jenkins_name";
	private String identityFieldsSeparator = "-";
	private String identityFieldName = "identity";

	private static final Logger LOGGER = Logger.getLogger(GlobalConjurConfiguration.class.getName());

	/**
	 * check the Token Duration for validity
	 *
	 * @param Jenkins AbstractItem anc
	 * @param Token   duration in sectonds
	 * @param Token   keyLifetimeInMinutes
	 * @return
	 */
	public FormValidation doCheckTokenDurarionInSeconds(@AncestorInPath AbstractItem anc,
			@QueryParameter("tokenDurarionInSeconds") String tokenDurarionInSeconds,
			@QueryParameter("keyLifetimeInMinutes") String keyLifetimeInMinutes) {
		LOGGER.log(Level.FINE, "Inside of doCheckTokenDurarionInSeconds()");
		try {
			int tokenttl = Integer.parseInt(tokenDurarionInSeconds);
			int keyttl = Integer.parseInt(keyLifetimeInMinutes);
			if (tokenttl > keyttl * 60) {
				LOGGER.log(Level.FINE, "Token cannot last longer than key");
				return FormValidation.error("Token cannot last longer than key");
			} else {
				return FormValidation.ok();
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Key lifetime and token duration must be numbers");
			return FormValidation.error("Key lifetime and token duration must be numbers");
		}
	}

	/**
	 * check the Auth WebService Id
	 *
	 * @param Jenkins AbstractItem anc
	 * @param Token   authWebServiceId
	 * @return
	 */
	public FormValidation doCheckAuthWebServiceId(@AncestorInPath AbstractItem anc,
			@QueryParameter("authWebServiceId") String authWebServiceId) {
		LOGGER.log(Level.FINE, "Inside of doCheckAuthWebServiceId()");
		if (StringUtils.isEmpty(authWebServiceId) || StringUtils.isBlank(authWebServiceId)) {
			LOGGER.log(Level.FINE, "Auth WebService Id should not be empty");
			return FormValidation.error("Auth WebService Id should not be empty");
		} else {
			return FormValidation.ok();
		}
	}

	/**
	 * check the JWT Audience
	 *
	 * @param Jenkins AbstractItem anc
	 * @param Token   jwtAudience
	 * @return
	 */
	public FormValidation doCheckJwtAudience(@AncestorInPath AbstractItem anc,
			@QueryParameter("jwtAudience") String jwtAudience) {
		LOGGER.log(Level.FINE, "Inside of doCheckJwtAudience()");
		if (StringUtils.isEmpty(jwtAudience) || StringUtils.isBlank(jwtAudience)) {
			LOGGER.log(Level.FINE, "JWT Audience field should not be empty");
			return FormValidation.error("JWT Audience field should not be empty");
		} else {
			return FormValidation.ok();
		}
	}

	/**
	 * check the Identity Format Fields From Token
	 *
	 * @param Jenkins AbstractItem anc
	 * @param Token   identityFormatFieldsFromToken
	 * @return
	 */
	public FormValidation doCheckIdentityFormatFieldsFromToken(@AncestorInPath AbstractItem anc,
			@QueryParameter("identityFormatFieldsFromToken") String identityFormatFieldsFromToken) {
		LOGGER.log(Level.FINE, "Inside of doCheckIdentityFormatField()");
		List<String> identityFields = Arrays.asList(identityFormatFieldsFromToken.split(","));
		// Check for duplicates in identityFields
		Set<String> uniqueIdentityFields = new HashSet<>(identityFields);
		if (StringUtils.isEmpty(identityFormatFieldsFromToken) || StringUtils.isBlank(identityFormatFieldsFromToken)) {
			LOGGER.log(Level.FINE, "IdentityFormatFieldsFromToken should not be empty");
			return FormValidation.error("IdentityFormatFieldsFromToken field should not be empty");
		}
		if (uniqueIdentityFields.size() < identityFields.size()) {
			LOGGER.log(Level.FINE, "Duplicate tokens found in IdentityFormatFieldsFromToken");
			return FormValidation.error("Duplicate tokens found in IdentityFormatFieldsFromToken");
		}
		return validateIdentityFormatFields(identityFields);
	}

	/**
	 * check the Identity Format Fields Seperator
	 *
	 * @param Jenkins AbstractItem anc
	 * @param JWT     IdentityFieldsSeparator
	 * @return
	 */
	public FormValidation doCheckIdentityFieldsSeparator(@AncestorInPath AbstractItem anc,
			@QueryParameter("identityFieldsSeparator") String identityFieldsSeparators) {
		LOGGER.log(Level.FINE, "Inside of doCheckIdentityFieldsSeparator()");

		if (StringUtils.isEmpty(identityFieldsSeparators) || StringUtils.isBlank(identityFieldsSeparators)) {
			LOGGER.log(Level.FINE, "Identity Fields Separator should not be empty");
			return FormValidation.error("Identity Fields Separator should not be empty");

		} else if (!identityFieldsSeparators.equals(identityFieldsSeparator)) {
			LOGGER.log(Level.FINE, "Identity Fields Separator should contain only single - ");
			return FormValidation.error("Identity Fields Separator should contain only single - ");
		} else {
			return FormValidation.ok();
		}
	}

	private FormValidation validateIdentityFormatFields(List<String> identityFields) {
		// Check for valid tokens
		Set<String> identityTokenSet = new HashSet<>(Arrays.asList("aud", "jenkins_parent_full_name", "jenkins_name",
				"userId", "fullName", "jenkins_full_name", "jenkins_parent_name"));

		List<String> identityFieldsList = Arrays.asList("aud", "userId", "fullName", "jenkins_full_name");
		List<String> commonFieldsList = Arrays.asList("jenkins_name", "jenkins_parent_full_name",
				"jenkins_parent_name");

		if (!identityFields.stream().allMatch(identityTokenSet::contains)) {
			String errorMsg = "IdentityFormatFieldsFromToken should contain any combination of the following fields with no space : aud,jenkins_parent_full_name,jenkins_name,userId,fullName,jenkins_full_name,jenkins_parent_name";
			LOGGER.log(Level.FINE, errorMsg);
			return FormValidation.error(errorMsg);
		}

		for (String field : identityFieldsList) {
			if (identityFields.contains(field) && !commonFieldsList.stream().anyMatch(identityFields::contains)) {
				return handleValidationError("jenkins_name, jenkins_parent_full_name, or jenkins_parent_name");
			}
		}
		// No validation errors
		return FormValidation.ok();

	}

	private FormValidation handleValidationError(String tokens) {
		LOGGER.log(Level.FINE, "IdentityFormatFieldsFromToken must contain at least one or more of " + tokens);
		return FormValidation.error("IdentityFormatFieldsFromToken must contain at least one or more of " + tokens);
	}

	/**
	 * @return the singleton instance , comment non-null due to trace exception
	 */
	// @Nonnull
	public static GlobalConjurConfiguration get() {
		GlobalConjurConfiguration result = null;
		try {
			result = GlobalConfiguration.all().get(GlobalConjurConfiguration.class);

			LOGGER.log(Level.FINE, "Inside GlobalConjurConfiguration get() result:  " + result);

			if (result == null) {
				throw new IllegalStateException();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * When Jenkins is restarted, load any saved configuration from disk.
	 */

	public GlobalConjurConfiguration() {
		// When Jenkins is restarted, load any saved configuration from disk.
		load();
	}

	/**
	 * @return ConjurConfiguration object
	 */
	public ConjurConfiguration getConjurConfiguration() {
		return conjurConfiguration;
	}

	/**
	 * @return boolean if JWKS is enabled
	 */
	public Boolean getEnableJWKS() {
		return enableJWKS;
	}

	/**
	 * @return boolean for enableContextAware CredentialStore
	 */

	public Boolean getEnableContextAwareCredentialStore() {
		return enableContextAwareCredentialStore;
	}

	/**
	 * @return Web Service ID for authentication
	 */
	public String getAuthWebServiceId() {
		return authWebServiceId;
	}

	/**
	 * set the Authentication WebService Id
	 */
	@DataBoundSetter
	public void setAuthWebServiceId(String authWebServiceId) {
		this.authWebServiceId = authWebServiceId;
		save();
	}

	/**
	 * @return the Identity FieldName
	 */
	public String getidentityFieldName() {
		return identityFieldName;
	}

	/**
	 * set the IdentityFieldName
	 */
	@DataBoundSetter
	public void setIdentityFieldName(String identityFieldName) {
		this.identityFieldName = identityFieldName;
		save();
	}

	/**
	 * @retrun IdentityFormatFieldsFromToken
	 */
	public String getIdentityFormatFieldsFromToken() {
		return identityFormatFieldsFromToken;
	}

	/**
	 * set the IdentityFormatFieldsFromToken
	 */
	@DataBoundSetter
	public void setIdentityFormatFieldsFromToken(String identityFormatFieldsFromToken) {
		this.identityFormatFieldsFromToken = identityFormatFieldsFromToken;
		save();
	}

	/**
	 * @return IdentityFieldsSeparator
	 */
	public String getIdentityFieldsSeparator() {
		return identityFieldsSeparator;
	}

	/**
	 * set the IdentityFieldsSeparator
	 */
	@DataBoundSetter
	public void setIdentityFieldsSeparator(String identityFieldsSeparator) {
		this.identityFieldsSeparator = identityFieldsSeparator;
		save();
	}

	/**
	 * @return the JWT Audience
	 */
	public String getJwtAudience() {
		return jwtAudience;
	}

	/**
	 * set the JWT Audience
	 */

	@DataBoundSetter
	public void setJwtAudience(String jwtAudience) {
		this.jwtAudience = jwtAudience;
		save();
	}

	/**
	 * @return the Key Life Time in Minutes
	 */
	public long getKeyLifetimeInMinutes() {
		return keyLifetimeInMinutes;
	}

	/**
	 * set the Key Life Time in Minutes
	 */
	@DataBoundSetter
	public void setKeyLifetimeInMinutes(long keyLifetimeInMinutes) {
		this.keyLifetimeInMinutes = keyLifetimeInMinutes;
		save();
	}

	/**
	 * @return the Token duration in seconds
	 */
	public long getTokenDurarionInSeconds() {
		return tokenDurarionInSeconds;
	}

	/**
	 * set the Token duration in seconds
	 */
	@DataBoundSetter
	public void setTokenDurarionInSeconds(long tokenDurarionInSeconds) {
		this.tokenDurarionInSeconds = tokenDurarionInSeconds;
		save();
	}

	/**
	 * set the Conjur Configuration parameters
	 */
	@DataBoundSetter
	public void setConjurConfiguration(ConjurConfiguration conjurConfiguration) {
		this.conjurConfiguration = conjurConfiguration;
		save();
	}

	/**
	 * set Enable JWKS option
	 */
	@DataBoundSetter
	public void setEnableJWKS(Boolean enableJWKS) {
		this.enableJWKS = enableJWKS;
		save();
	}

	/**
	 * set the EnablContextAwareCredentialStore selected value
	 */
	@DataBoundSetter
	public void setEnableContextAwareCredentialStore(Boolean enableContextAwareCredentialStore) {
		this.enableContextAwareCredentialStore = enableContextAwareCredentialStore;
		save();
	}

}
