package hamaster.gradesign.keydist.service.impl;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesign.keydist.dao.UserDAO;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private UserDAO userRepo;

    @Autowired
    public UserServiceImpl(UserDAO userRepo) {
        this.userRepo = requireNonNull(userRepo);
    }

    @Override
    public User login(String email, String password) {
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isPresent()) {
            String salt = User.formatDate(user.get().getRegDate());
            byte[] hash = Hash.sha512(new StringBuilder(password).append(salt).toString());
            if (Hex.hex(hash).equalsIgnoreCase(user.get().getPassword()))
                return user.get();
        }
        return null;
    }

    @Override
    public boolean isEmailExist(String email) {
        Optional<User> user = userRepo.findByEmail(email);
        return user.isPresent();
    }

    @Override
    public void register(User user, String password) {
        String salt = User.formatDate(user.getRegDate());
        String hash = Hex.hex(Hash.sha512(new StringBuilder(password).append(salt).toString()));
        user.setPassword(hash);
        user.setStatus(User.USER_REG);
        userRepo.save(user);
    }

    @Override
    public boolean isUsernameExist(String username) {
        Optional<User> user = userRepo.findByUsername(username);
        return user.isPresent();
    }
}
