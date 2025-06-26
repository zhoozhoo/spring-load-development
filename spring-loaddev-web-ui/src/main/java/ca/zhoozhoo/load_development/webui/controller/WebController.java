package ca.zhoozhoo.load_development.webui.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping(value = {"/", "/loads", "/loads/**", "/profile"})
    public String index(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("authenticated", true);
        } else {
            model.addAttribute("authenticated", false);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
