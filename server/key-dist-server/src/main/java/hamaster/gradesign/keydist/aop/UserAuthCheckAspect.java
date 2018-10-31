package hamaster.gradesign.keydist.aop;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.ClientService;
import hamaster.gradesign.keydist.service.UserService;

@Aspect
@Component
public class UserAuthCheckAspect {

    private UserService userService;

    @Autowired
    public UserAuthCheckAspect(UserService userService) {
        this.userService = requireNonNull(userService);
    }

    private final static Map<String, String> INVALID_USER_PASS = Map.of(
            "code", Integer.toString(ClientService.ERR_WRONG_PWD),
            "message", "Incorrect username or password");

    @Around("@annotation(UserAuth)")
    public Object checkUserNameAndPassword(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] arguments = joinPoint.getArgs();
        User owner = userService.loginWithUsername(arguments[0].toString(), arguments[1].toString());
        if (owner == null) {
            return INVALID_USER_PASS;
        }
        return joinPoint.proceed();
    }
}
