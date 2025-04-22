package com.s4mz.mailverification.service;

import com.s4mz.mailverification.dto.LoginUserDto;
import com.s4mz.mailverification.dto.RegisterUserDto;
import com.s4mz.mailverification.dto.VerifyUserDto;
import com.s4mz.mailverification.model.User;
import com.s4mz.mailverification.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public User signup(RegisterUserDto input){
        User user=new User();
        user.setUserName(input.getUserName());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input){
        User user=userRepository.findByUserName(input.getUserName())
                .orElseThrow(()->new UsernameNotFoundException("User not found."));

        if(!user.isEnabled()){
            throw new RuntimeException("Account not verified. Please verify your account.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUserName(),
                        input.getPassword()
                )
        );

        return user;
    }

    public void verifyUser(VerifyUserDto input){
        Optional<User> optionalUser=userRepository.findByUserName(input.getUserName());
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code expired.");
            }
            if(user.getVerificationCode().equals(input.getVerificationCode())){
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            }
            else {
                throw new RuntimeException("Invalid verification code.");
            }
        }
        else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public void resendVerificationCode(String userName){
        Optional<User> optionalUser=userRepository.findByUserName(userName);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
            sendVerificationEmail(user);
            userRepository.save(user);
        }
        else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    private String generateVerificationCode() {
        Random random=new Random();
        int code=random.nextInt(900000)+100000;
        return String.valueOf(code);
    }

    private void sendVerificationEmail(User user) {
        String subject="Account Verification";
        String verificationCode="Verification Code :"+generateVerificationCode();
        String htmlMessage="<html><body>"
                + "<h1>Account Verification</h1>"
                + "<p>Dear " + user.getUsername()
                + ",</p>"
                + "<p>Please use the following verification code to verify your account:</p>"
                + "<h2>" + user.getVerificationCode() + "</h2>"
                + "<p>This code will expire in 5 minutes.</p>"
                + "</body></html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject,htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


}
