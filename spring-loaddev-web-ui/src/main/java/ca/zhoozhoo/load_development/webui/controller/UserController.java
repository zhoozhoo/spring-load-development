package ca.zhoozhoo.load_development.webui.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            userInfo.put("authenticated", true);
            userInfo.put("username", authentication.getName());
            
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                userInfo.put("email", oauth2User.getAttribute("email"));
                userInfo.put("name", oauth2User.getAttribute("name"));
                userInfo.put("provider", oauth2User.getAttribute("iss"));
            }
        } else {
            userInfo.put("authenticated", false);
            userInfo.put("username", null);
        }
        
        return userInfo;
    }
}
