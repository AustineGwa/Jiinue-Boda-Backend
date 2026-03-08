package com.otblabs.jiinueboda.auth;

import com.auth0.jwt.JWT;
import com.otblabs.jiinueboda.exceptions.ExceptionsHandlerService;
import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import com.otblabs.jiinueboda.patners.PartnersService;
import com.otblabs.jiinueboda.security.SecurityConstants;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.users.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ExceptionsHandlerService exceptionsHandlerService;

    public AuthenticationController(UserService userService,
                                    BCryptPasswordEncoder bCryptPasswordEncoder,
                                    ExceptionsHandlerService exceptionsHandlerService
    ) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.exceptionsHandlerService = exceptionsHandlerService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse<Object>> adminLogin(@RequestBody Credentials credentials){

        try{
            SystemUser systemUser = userService.getByEmailOrPhone(credentials.getUser());

            if(systemUser != null){
                if(bCryptPasswordEncoder.matches(credentials.getPassword(),systemUser.getPassword())){
                    String token = JWT.create().withSubject(credentials.getUser())
                            .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                            .sign(HMAC512(SecurityConstants.SECRET.getBytes()));
                    return ResponseEntity.ok(new LoginResponse(token,systemUser));
                }else{
                    return  ResponseEntity.status(404).body(new LoginResponse<>("Error Wrong email or password combination", null));
                }

            } else {
                return ResponseEntity.status(404).body(new LoginResponse<>("Error user does not exist", null));
            }
        }catch (Exception exception){
            exception.printStackTrace();
            exceptionsHandlerService.saveExceptionToDb(exception.getMessage(),3);
            return ResponseEntity.internalServerError().build();
        }


    }

}
