package com.otblabs.jiinueboda.security.springsecurity;


import com.otblabs.jiinueboda.auth.LoggedInUser;
import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import com.otblabs.jiinueboda.investors.models.Investor;
import com.otblabs.jiinueboda.patners.Partner;
import com.otblabs.jiinueboda.patners.PartnersService;
import com.otblabs.jiinueboda.users.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private final PartnersService partnersService;

    private final InvestmentManagementService investmentManagementService;

    public UserDetailsServiceImpl(UserService userService, PartnersService partnersService, InvestmentManagementService investmentManagementService) {
        this.userService = userService;
        this.partnersService = partnersService;
        this.investmentManagementService = investmentManagementService;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList();

//            List<SimpleGrantedAuthority> grantedAuthorities = roleRepo.findAllByUserId(user.get().getId()).stream()
//            .map(r -> r.getRole()).map(r -> new SimpleGrantedAuthority(r)).collect(Collectors.toList());

        LoggedInUser loggedInUser = userService.getLoggedInUser(email);

        //check main user login
        if (loggedInUser != null){
            return new User(loggedInUser.getEmail(), String.valueOf(loggedInUser.getPassword()), grantedAuthorities);
        } else {
            //check partner login
            Partner partner = partnersService.getByEmailOrPhone(email);
            if(partner != null){
                return new User(partner.getContactEmail(), String.valueOf(partner.getPassword()), grantedAuthorities);
            }else{
                //check investor login
                Investor investor = investmentManagementService.getInvestorByUsername(email);
                if(investor != null){
                    return new User(investor.getUserName(), String.valueOf(investor.getPassword()), grantedAuthorities);
                }else{
                    //non of the above users
                throw new UsernameNotFoundException("Invalid user");
                }
            }
        }
    }



}

