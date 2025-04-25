package com.company.jmixpmsecurity.app;


import com.company.jmixpmsecurity.entity.User;
import com.company.jmixpmsecurity.security.DeveloperRole;
import com.company.jmixpmsecurity.security.DeveloperRowLevelRole;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RegistrationService {

    private final UnconstrainedDataManager unconstrainedDataManager;
    private final Emailer emailer;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UnconstrainedDataManager unconstrainedDataManager, Emailer emailer, PasswordEncoder passwordEncoder) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.emailer = emailer;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @return true if user with this email (or login) already exists.
     */
    public boolean checkUserAlreadyExist(String email) {
        List<User> users = unconstrainedDataManager.load(User.class)
                .query("select u from User u where u.email = :email " +
                        "or u.username= :email")
                .parameter("email", email)
                .list();
        return !users.isEmpty();
    }

    public User registerNewUser(String email, String firstName, String lastName) {
        User user = unconstrainedDataManager.create(User.class);
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        user.setActive(false);
        user.setNeedsActivation(true);

        user = unconstrainedDataManager.save(user);
        return user;
    }

    public String generateRandomActivationToken() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        ThreadLocalRandom current = ThreadLocalRandom.current();

        return current.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public void saveActivationToken(User user, String activationToken) {
        user = unconstrainedDataManager.load(User.class)
                .id(user.getId())
                .one();

        user.setActivationToken(activationToken);

        unconstrainedDataManager.save(user);
    }

    public void sendActivationEmail(User user) throws EmailException {
        user = unconstrainedDataManager.load(User.class)
                .id(user.getId())
                .one();

        String activationLink = "http://localhost:8080/activate?token=" + user.getActivationToken();

        String subject = "Registration in Jmix app";
        String body = String.format("Hello, %s %s \n Please finish your Registration \n Activation link: %s",
                user.getFirstName(),
                user.getLastName(),
                activationLink);

        emailer.sendEmail(
                EmailInfoBuilder.create()
                        .setFrom("jmixpm@sample.com")
                        .setAddresses(user.getEmail())
                        .setSubject(subject)
                        .setBody(body)
                        .build()
        );
    }

    @Nullable
    public User loadUserByActivationToken(String token) {
        User user = unconstrainedDataManager.load(User.class)
                .query("select u from User u where u.needsActivation = true and u.activationToken = :token")
                .parameter("token", token)
                .optional()
                .orElse(null);
        return user;
    }

    public void activateUser(User user, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setActivationToken(null);
        user.setActive(true);
        user.setNeedsActivation(false);

        RoleAssignmentEntity assignment1 = unconstrainedDataManager.create(RoleAssignmentEntity.class);
        assignment1.setUsername(user.getUsername());
        assignment1.setRoleType(RoleAssignmentRoleType.RESOURCE);
        assignment1.setRoleCode(DeveloperRole.CODE);

        RoleAssignmentEntity assignment2 = unconstrainedDataManager.create(RoleAssignmentEntity.class);
        assignment2.setUsername(user.getUsername());
        assignment2.setRoleType(RoleAssignmentRoleType.ROW_LEVEL);
        assignment2.setRoleCode(DeveloperRowLevelRole.CODE);


        unconstrainedDataManager.save(user, assignment1, assignment2);
    }
}