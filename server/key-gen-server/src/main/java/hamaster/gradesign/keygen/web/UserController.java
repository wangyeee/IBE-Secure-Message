package hamaster.gradesign.keygen.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {

    @RequestMapping("/register")
    public String registerPage() {
        return "register";
    }
}
