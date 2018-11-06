package hamaster.gradesign.keydist.service.impl;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesign.keydist.daemon.KeyGenClient;
import hamaster.gradesign.keydist.dao.IDRequestDAO;
import hamaster.gradesign.keydist.dao.UserDAO;
import hamaster.gradesign.keydist.dao.UserTokenDAO;
import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.entity.UserToken;
import hamaster.gradesign.keydist.service.UserService;
import hamaster.gradesign.keygen.IBECSR;

@Service
public class UserServiceImpl implements UserService {

    private UserDAO userRepo;
    private IDRequestDAO idRequestRepo;
    private KeyGenClient client;
    private UserTokenDAO userTokenDAO;

    @Autowired
    public UserServiceImpl(UserDAO userRepo, IDRequestDAO idRequestRepo, UserTokenDAO userTokenDAO, KeyGenClient client) {
        this.userRepo = requireNonNull(userRepo);
        this.userTokenDAO = requireNonNull(userTokenDAO);
        this.idRequestRepo = requireNonNull(idRequestRepo);
        this.client = requireNonNull(client);
    }

    public User loginWithUsername(String username, String password) {
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isPresent()) {
            return checkPassword(user.get(), password);
        }
        return null;
    }

    @Override
    public User loginWithEmail(String email, String password) {
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isPresent()) {
            return checkPassword(user.get(), password);
        }
        return null;
    }

    @Override
    public User loginWithToken(String username, String uuid) {
        Optional<UserToken> token = userTokenDAO.findOneByUUID(uuid);
        if (token.isPresent()) {
            if (token.get().getUser().getUsername().equals(username)) {
                return token.get().getUser();
            }
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
        req.setStatus(IBECSR.APPLICATION_STARTED);
        req.setPassword(Hex.hex(Hash.sha512(password)));
        req.setPasswordToKeyGen(client.encryptSessionKeyForSystem(password.getBytes()));
        idRequestRepo.save(req);
    }

    @Override
    public boolean isUsernameExist(String username) {
        Optional<User> user = userRepo.findByUsername(username);
        return user.isPresent();
    }
    
    @Override
    public UserToken appLogin(String username, String password) {
        return appLogin(username, password, null);
    }
    
    @Override
    public UserToken appLogin(String username, String password, String description) {
        User user = loginWithUsername(username, password);
        if (user != null) {
            UserToken token = new UserToken();
            token.setUser(user);
            token.setDescription(description);
            token.setGenerationDate(new Date());
            token.setUuid(UUID.randomUUID().toString());
            userTokenDAO.save(token);
            return token;
        }
        return null;
    }

    private User checkPassword(User user, String password) {
        String salt = User.formatDate(user.getRegDate());
        byte[] hash = Hash.sha512(new StringBuilder(password).append(salt).toString());
        if (diffArray(hash, Hex.unhex(user.getPassword())))
            return null;
        return user;
    }

    /**
     * Compare two byte array with constant timing
     * @param a first byte array
     * @param b second byte array
     * @return true if a != b, false otherwise
     */
    private boolean diffArray(byte[] a, byte[] b) {
        int sum, size;
        if (a.length > b.length) {
            size = b.length;
            sum = a.length - b.length;
        } else {
            size = a.length;
            sum = b.length - a.length;
        }
        for (int i = 0; i < size; i++) {
            int j = a[i] - b[i];
            sum += j * j;
        }
        return sum != 0;
    }
}
