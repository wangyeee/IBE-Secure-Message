package hamaster.gradesign.keydist.service.impl;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesign.keydist.daemon.KeyGenClient;
import hamaster.gradesign.keydist.dao.IDRequestDAO;
import hamaster.gradesign.keydist.dao.UserDAO;
import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.UserService;
import hamaster.gradesign.keygen.IBECSR;

@Service
public class UserServiceImpl implements UserService {

    private UserDAO userRepo;
    private IDRequestDAO idRequestRepo;
    private KeyGenClient client;

    @Autowired
    public UserServiceImpl(UserDAO userRepo, IDRequestDAO idRequestRepo, KeyGenClient client) {
        this.userRepo = requireNonNull(userRepo);
        this.idRequestRepo = requireNonNull(idRequestRepo);
        this.client = requireNonNull(client);
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
        IDRequest req = new IDRequest();
        req.setApplicant(user);
        req.setApplicationDate(new Date());
        req.setIdentityString(user.getEmail());
        req.setIbeSystemId(client.getCurrentSystemID());
        req.setStatus(IBECSR.APPLICATION_NOT_VERIFIED);
        req.setPassword(Hex.hex(Hash.sha512(password)));
        req.setPasswordToKeyGen(client.encryptSessionKeyForSystem(password.getBytes()));
        idRequestRepo.save(req);
    }

    @Override
    public boolean isUsernameExist(String username) {
        Optional<User> user = userRepo.findByUsername(username);
        return user.isPresent();
    }
}
