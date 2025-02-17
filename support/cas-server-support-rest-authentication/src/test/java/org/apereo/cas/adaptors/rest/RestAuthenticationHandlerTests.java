package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRestAuthenticationAutoConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasRestAuthenticationAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAutoConfiguration.class
},
    properties = "cas.authn.rest[0].uri=http://localhost:8081/authn")
@Tag("RestfulApiAuthentication")
class RestAuthenticationHandlerTests {
    @Autowired
    @Qualifier("restAuthenticationHandler")
    private BeanContainer<AuthenticationHandler> authenticationHandler;

    private AuthenticationHandler getFirstHandler() {
        return authenticationHandler.first();
    }

    @Test
    void verifySuccess() throws Throwable {
        val instant = Instant.now(Clock.systemUTC()).plus(10, ChronoUnit.DAYS);
        val formatted = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(instant);

        val headers = new HashMap<String, String>();
        headers.put(RestAuthenticationHandler.HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE, formatted);
        headers.put(RestAuthenticationHandler.HEADER_NAME_CAS_WARNING, "warning1");

        try (val webServer = new MockWebServer(8081, PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser"), headers, HttpStatus.OK)) {
            webServer.start();
            val res = getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class));
            assertEquals("casuser", res.getPrincipal().getId());
        }
    }

    @Test
    void verifyNoPrincipal() throws Throwable {
        try (val webServer = new MockWebServer(8081, StringUtils.EMPTY)) {
            webServer.start();
            assertThrows(FailedLoginException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }

    @Test
    void verifyDisabledAccount() throws Throwable {
        try (val webServer = new MockWebServer(8081, HttpStatus.FORBIDDEN)) {
            webServer.start();
            assertThrows(AccountDisabledException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }

    @Test
    void verifyUnauthorized() throws Throwable {
        try (val webServer = new MockWebServer(8081, HttpStatus.UNAUTHORIZED)) {
            webServer.start();
            assertThrows(FailedLoginException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }

    @Test
    void verifyOther() throws Throwable {
        try (val webServer = new MockWebServer(8081, HttpStatus.REQUEST_TIMEOUT)) {
            webServer.start();
            assertThrows(FailedLoginException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }

    @Test
    void verifyLocked() throws Throwable {
        try (val webServer = new MockWebServer(8081, HttpStatus.LOCKED)) {
            webServer.start();
            assertThrows(AccountLockedException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }

    @Test
    void verifyConditionReq() throws Throwable {
        try (val webServer = new MockWebServer(8081, HttpStatus.PRECONDITION_REQUIRED)) {
            webServer.start();
            assertThrows(AccountPasswordMustChangeException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }

    @Test
    void verifyConditionFail() throws Throwable {
        try (val webServer = new MockWebServer(8081, HttpStatus.PRECONDITION_FAILED)) {
            webServer.start();
            assertThrows(AccountExpiredException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }

    @Test
    void verifyNotFound() throws Throwable {
        try (val webServer = new MockWebServer(8081, HttpStatus.NOT_FOUND)) {
            webServer.start();
            assertThrows(AccountNotFoundException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }
}



