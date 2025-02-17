package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RenewAuthenticationRequestCheckActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowAuthenticationActions")
class RenewAuthenticationRequestCheckActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyProceed() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val strategy = new DefaultSingleSignOnParticipationStrategy(servicesManager, casProperties.getSso(),
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        val action = new RenewAuthenticationRequestCheckAction(strategy);
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROCEED, action.execute(context).getId());
    }

    @Test
    void verifyRenew() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val strategy = new DefaultSingleSignOnParticipationStrategy(servicesManager, casProperties.getSso(),
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        val action = new RenewAuthenticationRequestCheckAction(strategy);
        assertEquals(CasWebflowConstants.TRANSITION_ID_RENEW, action.execute(context).getId());
    }


}
