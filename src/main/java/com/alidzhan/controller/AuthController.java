package com.alidzhan.controller;

import com.alidzhan.config.JwtProvider;
import com.alidzhan.modal.TwoFactorOTP;
import com.alidzhan.modal.User;
import com.alidzhan.repository.UserRepository;
import com.alidzhan.response.AuthResponse;
import com.alidzhan.service.CustomUserDetailsService;
import com.alidzhan.service.EmailService;
import com.alidzhan.service.TwoFactorOtpService;
import com.alidzhan.utils.OtpUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final EmailService emailService;
    private UserRepository userRepository;
    private CustomUserDetailsService customUserDetailsService;
    private TwoFactorOtpService twoFactorOtpService;
    public AuthController(UserRepository userRepository, CustomUserDetailsService customUserDetailsService, TwoFactorOtpService twoFactorOtpService, EmailService emailService) {
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
        this.twoFactorOtpService = twoFactorOtpService;
        this.emailService = emailService;
    }
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {
        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if(isEmailExist != null){
            throw new Exception("Email already exist");
        }
        User newUser = new User();
        newUser.setFullName(user.getFullName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        User savedUser = userRepository.save(newUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = JwtProvider.generateToken(auth);

        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("register successfully");


        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) throws Exception {

        String userName = user.getEmail();
        String password = user.getPassword();

        Authentication auth = authenticate(userName,password);
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = JwtProvider.generateToken(auth);

        User authUser = userRepository.findByEmail(userName);
        if(user.getTwoFactorAuth().isEnabled()){
            AuthResponse res = new AuthResponse();
            res.setMessage("2FA is enabled");
            res.setTwoFactorAuthEnabled(true);
            String otp = OtpUtils.generateOtp();
            TwoFactorOTP oldTwoFactorOtp = twoFactorOtpService.findByUser(authUser.getId());
            if(oldTwoFactorOtp != null){
                twoFactorOtpService.deleteTwoFactorOtp(oldTwoFactorOtp);
            }
            TwoFactorOTP newTwoFactorOTP = twoFactorOtpService.createTwoFactorOtp(authUser,otp,jwt);
            emailService.SendVerificationOtpEmail(userName,otp);
            res.setSession(newTwoFactorOTP.getId());
            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
        }
        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("login successfully");


        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    private Authentication authenticate(String userName, String password) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userName);
        if(userDetails == null) {
            throw new BadCredentialsException("User not found");
        }
        if(!password.equals(userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails,password,userDetails.getAuthorities());
    }

    public ResponseEntity<AuthResponse> verifySignIn(@PathVariable String otp, @RequestParam String id) throws Exception {
        TwoFactorOTP twoFactorOTP = twoFactorOtpService.findById(id);

        if(twoFactorOtpService.verifyTwoFactorOtp(twoFactorOTP,otp)){
            AuthResponse authResponse = new AuthResponse();
            authResponse.setMessage("Two factor authentication verified");
            authResponse.setTwoFactorAuthEnabled(true);
            authResponse.setJwt(twoFactorOTP.getJwt());
            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        }
        throw new Exception("invalid otp");
    }
}
