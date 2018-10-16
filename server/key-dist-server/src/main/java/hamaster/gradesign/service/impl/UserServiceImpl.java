package hamaster.gradesign.service.impl;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.dao.UserDAO;
import hamaster.gradesign.entity.User;
import hamaster.gradesign.ibe.util.Hex;
import hamaster.gradesign.service.UserService;

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
    public void save(User user) {
        userRepo.save(requireNonNull(user));
    }
}
