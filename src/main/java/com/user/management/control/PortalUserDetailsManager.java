package com.user.management.control;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.user.management.control.exception.InvalidPasswordException;
import com.user.management.control.exception.PasswordUsernameMatchException;
import com.user.management.control.exception.SamePasswordException;
import com.user.management.control.exception.UserNotFoundException;
import com.user.management.control.exception.UsernameAlreadyExistsException;
import com.user.management.entity.PortalPermissions;
import com.user.management.entity.PortalUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PortalUserDetailsManager implements UserDetailsService, UserDetailsPasswordService {

    private final PortalUserDetailsRepostory userDetailsRepostory;
    private final ApplicationContext context;

    @Override
    public PortalUserDetails loadUserByUsername(String username) {
        return userDetailsRepostory.findByUsername(username)
                .orElseThrow(throwIfUsernameNotFound(username));
    }

    public PortalUserDetails findUserById(UUID id) {
        return userDetailsRepostory.findById(id)
                .orElseThrow(throwIfUserNotFound("id", id));
    }

    public List<PortalUserDetails> findAllUsers() {
        return userDetailsRepostory.findAll();
    }

    @Override
    public PortalUserDetails updatePassword(UserDetails user, String newPassword) {
        log.info("Updating password..");
        userDetailsRepostory.updatePasswordByUsername(newPassword, user.getUsername());
        return loadUserByUsername(user.getUsername());
    }

    public void changePassword(UserDetails user, String newPassword) {
        DaoAuthenticationProvider authenticationProvider = (DaoAuthenticationProvider) context
                .getBean(AuthenticationProvider.class);
        authenticationProvider.setCompromisedPasswordChecker(null);
        authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername().trim(), user.getPassword().trim()));

        if (newPassword.equalsIgnoreCase(user.getPassword().trim()))
            throw new SamePasswordException();

        throwIfUsernamePasswordMatch(user.getUsername().trim(), newPassword);
        assertIfValidPassword(newPassword);
        throwIfPasswordCompromised(newPassword);

        updatePassword(user, encodePassword(newPassword));
    }

    public void createUser(PortalUserDetails user) {
        if (userExists(user.getUsername().trim()))
            throw new UsernameAlreadyExistsException();

        String password = user.getPassword().trim();

        throwIfUsernamePasswordMatch(user.getUsername().trim(), password);
        assertIfValidPassword(password);
        throwIfPasswordCompromised(password);

        user.setUsername(user.getUsername().trim());
        user.setName(user.getName().trim());
        user.setPassword(encodePassword(password));
        user.setPermissions(PortalPermissions.USER);

        userDetailsRepostory.save(user);
    }

    public void registerConversation(UUID conversationId, PortalUserDetails userDetails) {
        var conversationIds = new ArrayList<>(userDetails.getConversationIds());
        conversationIds.add(conversationId);
        userDetails.setConversationIds(conversationIds);
        userDetailsRepostory.save(userDetails);
    }

    public void deleteUser(String username) {
        if (userExists(username))
            userDetailsRepostory.deleteByUsername(username);
    }

    public void changeName(PortalUserDetails userDetails, String oldName, String newName) {
        if (!userDetails.getName().equals(oldName))
            throwIfUserNotFound("name", oldName);

        userDetailsRepostory.updateNameByUsername(newName, userDetails.getUsername());
    }

    public boolean userExists(String username) {
        return userDetailsRepostory.existsByUsername(username);
    }

    private String encodePassword(String password) {
        log.info("Encoding password..");
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        return passwordEncoder.encode(password);
    }

    private Supplier<UsernameNotFoundException> throwIfUsernameNotFound(String username) {
        return () -> new UsernameNotFoundException("Unknown username '" + username + "'");
    }

    private Supplier<UserNotFoundException> throwIfUserNotFound(String field, Object value) {
        return () -> new UserNotFoundException(field, value);
    }

    private void throwIfPasswordCompromised(String newPassword) {
        log.info("Checking if password is compromised..");
        CompromisedPasswordChecker passwordChecker = context.getBean(CompromisedPasswordChecker.class);
        if (passwordChecker.check(newPassword).isCompromised())
            throw new CompromisedPasswordException("*".repeat(newPassword.length()) + " has been compromised already");
    }

    private void assertIfValidPassword(String newPassword) {
        log.info("Checking if password is strong..");
        boolean isValidPassword = newPassword.length() >= 8 && newPassword.matches(".*\\d.*")
                && newPassword.matches(".*[A-Z].*") && newPassword.matches(".*\\W.*")
                && newPassword.matches(".*[a-z].*") && !newPassword.contains(" ");
        if (!isValidPassword)
            throw new InvalidPasswordException();
    }

    private void throwIfUsernamePasswordMatch(String username, String password) {
        log.info("Checking if username and password match..");
        if (username.equalsIgnoreCase(password))
            throw new PasswordUsernameMatchException();
    }
}
