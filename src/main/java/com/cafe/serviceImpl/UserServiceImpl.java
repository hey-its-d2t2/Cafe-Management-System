package com.cafe.serviceImpl;

import com.cafe.JWT.CustomerUserDetailService;
import com.cafe.JWT.JwtFilter;
import com.cafe.JWT.JwtUtil;
import com.cafe.POJO.User;
import com.cafe.constents.CafeConstants;
import com.cafe.dao.UserDAO;
import com.cafe.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.cafe.service.UserService;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDAO userDAO;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CustomerUserDetailService customerUserDetailService;
    @Autowired
    JwtUtil jwtUtil;
    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap){
        log.info("Inside signUp {}",requestMap);
        try {


            if (validateSignUpMap(requestMap)) {
                User user = userDAO.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDAO.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully registered", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Email already exists", HttpStatus.BAD_REQUEST);
                }

            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    private boolean validateSignUpMap(Map<String, String> requestMap){
        if(requestMap.containsKey("name")&&
                requestMap.containsKey("contactNumber")&&
                requestMap.containsKey("password")&&
                requestMap.containsKey("email"))
        {
            return true;
        }

        return false;
    }

    private  User getUserFromMap(Map<String, String> requestMap)
    {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus(CafeConstants.USER_STATUS_FALSE);
        user.setRole(CafeConstants.USER_ROLE);
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
       log.info("Inside login method");
       try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
            if(authentication.isAuthenticated()){
                if(customerUserDetailService.getUserDetails().getStatus().equalsIgnoreCase("true"))
                {
                    return new ResponseEntity<String>( "{\"token\":\""+
                        jwtUtil.generateToken(customerUserDetailService.getUserDetails().getEmail(),
                                customerUserDetailService.getUserDetails().getRole()) + "\"}",
                        HttpStatus.OK);

                }
                else {
                    return new ResponseEntity<String>("{\"message\":\""+"Wait for admin to approve"+"\"}", HttpStatus.BAD_REQUEST);
                }
            }
       }catch (Exception e) {
           e.printStackTrace();
       }
        return new ResponseEntity<String>("{\"message\":\""+"Bad credential"+"\"}", HttpStatus.BAD_REQUEST);

    }
}
