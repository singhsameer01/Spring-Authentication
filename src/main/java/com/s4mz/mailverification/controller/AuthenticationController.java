package com.s4mz.mailverification.controller;

import com.s4mz.mailverification.dto.LoginUserDto;
import com.s4mz.mailverification.dto.RegisterUserDto;
import com.s4mz.mailverification.dto.VerifyUserDto;
import com.s4mz.mailverification.model.User;
import com.s4mz.mailverification.response.LoginResponse;
import com.s4mz.mailverification.service.AuthenticationService;
import com.s4mz.mailverification.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto){
        User registeredUser= authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public  ResponseEntity<LoginResponse> responseEntity(@RequestBody LoginUserDto loginUserDto){
        User authenticatedUser= authenticationService.authenticate(loginUserDto);
        String jwtToken=jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse=new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify")
    public  ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto){
        try {
            authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Account verified successfully");
        }
        catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String userName){
        try {
            authenticationService.resendVerificationCode(userName);
            return ResponseEntity.ok("Verification code sent.");
        }
        catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
