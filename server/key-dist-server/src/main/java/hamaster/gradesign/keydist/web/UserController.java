package hamaster.gradesign.keydist.web;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.validator.routines.EmailValidator;
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
    private EmailValidator emailValidator;

    @Autowired
    public UserController(UserService userService) {
        this.userService = requireNonNull(userService);
        this.emailValidator = EmailValidator.getInstance();
    }

    @GetMapping(value = "/logout")
    public String logout(Model model, HttpSession session) {
        Object user = session.getAttribute("user");
        if (user != null) {
            session.removeAttribute("user");
        }
        model.addAttribute("messages", List.of("You have logged out"));
        return LOGIN;
    }

    @GetMapping(value = "/login")
    public String loginPage() {
        return LOGIN;
    }

    @PostMapping(value = "/login")
    public String login(@RequestParam(name = "username", required = true) String usernameOrEmail,
            @RequestParam(name = "password", required = true) String password,
            Model model, HttpSession session) {
        User user = null;
        if (emailValidator.isValid(usernameOrEmail)) {
            user = userService.loginWithEmail(usernameOrEmail, password);
        } else {
            user = userService.loginWithUsername(usernameOrEmail, password);
        }
        if (user == null) {
            model.addAttribute("messages", List.of("Invalid username or password"));
            return LOGIN;
        }
        session.setAttribute("user", user);
        return "redirect:/id";
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
