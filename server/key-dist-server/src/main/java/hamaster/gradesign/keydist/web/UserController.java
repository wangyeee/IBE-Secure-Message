package hamaster.gradesign.keydist.web;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.UserService;

@Controller
public class UserController {

    private final static String REGISTER = "reg";
    private final static String LOGIN = "login";

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = requireNonNull(userService);
    }

    @GetMapping(value = "/register")
    public String registerPage() {
        return REGISTER;
    }

    @PostMapping(value = "/register")
    public String register(@RequestParam(name = "username", required = true) String username,
            @RequestParam(name = "email", required = true) String email,
            @RequestParam(name = "password", required = true) String password,
            @RequestParam(name = "confirmpassword", required = true) String confirmPassword, Model model) {
        String url = null;
        List<String> messages = new ArrayList<String>();
        if (userService.isUsernameExist(username)) {
            messages.add(String.format("Username: %s already exist.", username));
            url = REGISTER;
        }
        if (userService.isEmailExist(email)) {
            messages.add(String.format("Email: %s is already used.", email));
            url = REGISTER;
        }
        if (!password.equals(confirmPassword)) {
            messages.add("Password not match.");
            url = REGISTER;
        }
        if (url == null) {
            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setRegDate(new Date());
            userService.register(user, password);
            messages.add("Registration completed successfully. Please login.");
            url = LOGIN;
        }
        model.addAttribute("messages", messages);
        return url;
    }
}
