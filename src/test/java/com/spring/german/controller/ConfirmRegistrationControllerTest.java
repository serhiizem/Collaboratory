package com.spring.german.controller;

import com.spring.german.entity.User;
import com.spring.german.entity.VerificationToken;
import com.spring.german.repository.UserRepository;
import com.spring.german.service.DefaultVerificationTokenService;
import com.spring.german.service.interfaces.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Locale;

import static com.spring.german.util.Endpoints.GALLERY_PAGE;
import static com.spring.german.util.Endpoints.TOKEN_EXPIRED_ERROR_PAGE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(ConfirmRegistrationController.class)
@WebAppConfiguration
public class ConfirmRegistrationControllerTest {

    private static final User VALID_USER = new User();
    private static final String VALID_TOKEN = "non-expired";

    private VerificationToken validVerificationToken;
    private VerificationToken expiredVerificationToken;

    @Autowired private MockMvc mvc;
    @Autowired private MockHttpSession session;

    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private UserRepository userRepository;
    @MockBean private UserService userService;
    @MockBean private DefaultVerificationTokenService verificationTokenService;

    @Before
    public void setUp() {
        validVerificationToken = new VerificationToken(VALID_TOKEN, VALID_USER);
        validVerificationToken.setExpiryDate(LocalDate.now().plusDays(1));
        expiredVerificationToken = new VerificationToken(VALID_TOKEN, VALID_USER);
        expiredVerificationToken.setExpiryDate(LocalDate.now().minusDays(1));
    }

    @Test
    public void shouldUpdateUserStateIfRegistrationTokenHasNotExpired() throws Exception {
        given(verificationTokenService.getEntityByKey(anyString()))
                .willReturn(validVerificationToken);

        mvc.perform(get("/registrationConfirm")
                .with(user("RedSulfur")
                        .password("pass")
                        .roles("USER", "ADMIN"))
                        .requestAttr("token", VALID_TOKEN)
                        .param("token", VALID_TOKEN)
                        .session(session).locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name(GALLERY_PAGE));

        verify(verificationTokenService, times(1)).getEntityByKey(anyString());
        verify(userService, times(1)).updateUserState(anyObject());
    }

    @Test
    public void shouldNotUpdateUserStateIfTokenHasExpired() throws Exception {
        given(verificationTokenService.getEntityByKey(anyString()))
                .willReturn(expiredVerificationToken);

        mvc.perform(get("/registrationConfirm")
                .with(user("RedSulfur")
                        .password("pass")
                        .roles("USER", "ADMIN"))
                .requestAttr("token", VALID_TOKEN)
                .param("token", VALID_TOKEN)
                .session(session).locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name(TOKEN_EXPIRED_ERROR_PAGE));

        verify(verificationTokenService, times(1)).getEntityByKey(anyString());
        verify(userService, times(0)).updateUserState(anyObject());
    }
}