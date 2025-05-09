package com.company.jmixpmsecurity.view.public_.useractivation;

import com.company.jmixpmsecurity.app.RegistrationService;
import com.company.jmixpmsecurity.entity.User;
import com.company.jmixpmsecurity.security.DatabaseUserRepository;
import com.company.jmixpmsecurity.view.login.LoginView;
import com.company.jmixpmsecurity.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.authentication.AuthDetails;
import io.jmix.securityflowui.authentication.LoginViewSupport;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@AnonymousAllowed
@Route(value = "activate")
@ViewController("UserActivationView")
@ViewDescriptor("user-activation-view.xml")
public class UserActivationView extends StandardView {
    private static final Logger log = LoggerFactory.getLogger(UserActivationView.class);

    @Autowired
    private RegistrationService registrationService;
    @ViewComponent
    private MessageBundle messageBundle;
    @Autowired
    private ViewValidation viewValidation;
    @Autowired
    private Notifications notifications;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private LoginViewSupport loginViewSupport;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @ViewComponent
    private H3 notFoundHeader;
    @ViewComponent
    private H3 welcomeHeader;
    @ViewComponent
    private FormLayout form;
    @ViewComponent
    private JmixPasswordField passwordField;
    @ViewComponent
    private JmixButton activateBtn;
    @ViewComponent
    private JmixButton returnToLoginBtn;

    private User user;
    private boolean initialized = false;
    @Autowired
    private UserRepository userRepository;

    @Subscribe
    public void onQueryParametersChange(final QueryParametersChangeEvent event) {
        String receivedActivationToken = event.getQueryParameters()
                .getSingleParameter("token")
                .orElse(null);

        if (StringUtils.isNotEmpty(receivedActivationToken)) {
            user = registrationService.loadUserByActivationToken(receivedActivationToken);
        } else {
            user = null;
        }

        updateUI();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (!initialized) {
            updateUI();
        }
    }

    @Subscribe(id = "returnToLoginBtn", subject = "clickListener")
    public void onReturnToLoginBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, LoginView.class)
                .navigate();
    }

    @Subscribe(id = "activateBtn", subject = "clickListener")
    public void onActivateBtnClick(final ClickEvent<JmixButton> event) {
        if (!validateFields()) {
            return;
        }

        String password = passwordField.getValue();
        registrationService.activateUser(user, password);

//        loginByPassword(password);

        loginAsTrusted();
    }

    private void loginByPassword(String password) {
        try {
            loginViewSupport.authenticate(
                    AuthDetails.of(user.getUsername(), password)
            );
        } catch (final BadCredentialsException | DisabledException | LockedException | AccessDeniedException e) {
            log.warn("Login failed for user '{}': {}", user.getUsername(), e.toString());
            notifications.create("Activation failed!")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    // mostly copied from io.jmix.securityui.authentication.LoginScreenSupport
    private void loginAsTrusted() {
        log.info("Login without password");

        user = (User) userRepository.loadUserByUsername(user.getUsername());

        Authentication authentication = new SystemAuthenticationToken(user, user.getAuthorities());

        try {
            authenticationManager.authenticate(authentication);
        } catch (AuthenticationException e) {
            notifications.create("Activation error!")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }

        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        VaadinServletResponse response = VaadinServletResponse.getCurrent();

        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

        SecurityContextHelper.setAuthentication(authentication);

        viewNavigators.view(this, MainView.class)
                .navigate();
    }

    private void updateUI() {
        boolean success = user != null;

        welcomeHeader.setVisible(success);
        form.setVisible(success);
        activateBtn.setVisible(success);

        notFoundHeader.setVisible(!success);
        returnToLoginBtn.setVisible(!success);

        if (user != null) {
            welcomeHeader.setText(messageBundle.formatMessage("welcomeHeader.text", user.getFirstName(), user.getLastName()));
        }

        initialized = true;
    }

    public boolean validateFields() {
        ValidationErrors validationErrors = viewValidation.validateUiComponents(form);
        if (!validationErrors.isEmpty()) {
            viewValidation.showValidationErrors(validationErrors);
            return false;
        }
        return true;
    }
}