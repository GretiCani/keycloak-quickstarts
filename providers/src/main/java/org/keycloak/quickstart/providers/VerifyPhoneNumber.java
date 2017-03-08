package org.keycloak.quickstart.providers;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Created by st on 08/03/17.
 */
public class VerifyPhoneNumber implements RequiredActionProvider, RequiredActionFactory {

    private static final Logger LOGGER = Logger.getLogger(VerifyPhoneNumber.class);

    private static final String PHONE_NUMBER = "phone_number";
    private static final String PHONE_NUMBER_VERIFIED = "phone_number_verified";

    // Required as there's not currently a way to remove the verified field if number changes
    private static final String PHONE_NUMBER_VERIFIED_NUMBER = "phone_number_verified_number";

    public static final String CODE_KEY = VerifyPhoneNumber.class.getName() + ".code";
    public static final String NUMBER_KEY = VerifyPhoneNumber.class.getName() + ".number";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        if (isSet(context) && !isVerified(context)) {
            context.getUser().addRequiredAction("verify-phone-number");
            LOGGER.info("User is required to verify phone number");
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        String code = generateCode();
        String phoneNumber = getPhoneNumber(context);

        context.getUserSession().setNote(NUMBER_KEY, phoneNumber);
        context.getUserSession().setNote(CODE_KEY, code);

        // Will be an SPI eventually: session.getProvider(SmsSender.class).send(...)
        new DummySmsSenderProvider().send(phoneNumber, code);

        Response challenge = context.form().createForm("login-verify-phone.ftl");
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        String code = (context.getHttpRequest().getDecodedFormParameters().getFirst("code"));

        String sentCode = context.getUserSession().getNote(CODE_KEY);
        String sentNumber = context.getUserSession().getNote(NUMBER_KEY);

        if (code.equals(sentCode)) {
            context.getUserSession().removeNote(CODE_KEY);

            context.getUser().setSingleAttribute(PHONE_NUMBER_VERIFIED, "true");
            context.getUser().setSingleAttribute(PHONE_NUMBER_VERIFIED_NUMBER, sentNumber);

            context.success();
        } else {
            Response challenge = context.form().setError("Wrong code").createForm("login-verify-phone.ftl");
            context.challenge(challenge);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getDisplayText() {
        return "Verify Phone Number";
    }

    @Override
    public String getId() {
        return "verify-phone-number";
    }

    private boolean isVerified(RequiredActionContext context) {
        if (Boolean.parseBoolean(context.getUser().getFirstAttribute(PHONE_NUMBER_VERIFIED))) {
            String phoneNumber = getPhoneNumber(context);
            String phoneNumberVerified = getPhoneNumberVerified(context);
            return Objects.equals(phoneNumber, phoneNumberVerified);
        } else {
            return false;
        }
    }

    private boolean isSet(RequiredActionContext context) {
        return getPhoneNumber(context) != null ? true : false;
    }

    private String getPhoneNumber(RequiredActionContext context) {
        String phoneNumber = context.getUser().getFirstAttribute(PHONE_NUMBER);
        return phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber : null;
    }

    private String getPhoneNumberVerified(RequiredActionContext context) {
        String phoneNumber = context.getUser().getFirstAttribute(PHONE_NUMBER_VERIFIED_NUMBER);
        return phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber : null;
    }

    private String generateCode() {
        SecureRandom r = new SecureRandom();
        return String.format("%04d", r.nextInt(9999));
    }

}
