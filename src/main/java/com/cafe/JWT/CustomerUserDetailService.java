package com.cafe.JWT;

import com.cafe.dao.UserDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
@Slf4j
@Service
public class CustomerUserDetailService implements UserDetailsService {

    @Autowired
    UserDAO userDAO;

    private  com.cafe.POJO.User userDetails;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Inside loadUserByUsername {}",username);
        userDetails = userDAO.findByEmailId(username);
        if(!Objects.isNull(userDetails))
            return new User(userDetails.getEmail(),userDetails.getPassword(), new ArrayList<>());
        else
            throw new UsernameNotFoundException("User not found.");
    }

    public com.cafe.POJO.User getUserDetails(){
        return userDetails;
    }
}
