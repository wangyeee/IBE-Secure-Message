package hamaster.gradesign.keydist.daemon;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import hamaster.gradesgin.ibe.IBECipherText;
import hamaster.gradesgin.ibe.IBEPlainText;
import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesgin.util.IBECapsule;
import hamaster.gradesgin.util.IBECapsuleAESImpl;
import hamaster.gradesign.keydist.client.Encoder;
import hamaster.gradesign.keygen.IBECSR;
import hamaster.gradesign.keygen.IdentityDescription;
import hamaster.gradesign.keygen.SimpleRESTResponse;

@Service
public class KeyGenClient {

    @Value("${hamaster.gradesign.keydist.genserver}")
    private String keyGenServereURL;

    @Value("${hamaster.gradesign.keydist.system}")
    private String systemIDStr;

    @Value("${hamaster.gradesign.keydist.server_id}")
    private String serverID;

    @Value("${hamaster.gradesign.keydist.server_key_valid:1000000000000}")
    private Long serverKeyValidPeriod;

    @Value("${hamaster.gradesign.keydist.server_key_dir}")
    private String serverKeyLocation;

    private final static String SERVER_KEY_FILE = "ibedist.key";
    private final static String SERVER_KEY_FILE_CONTENT = "content";
    private final static String SERVER_KEY_FILE_CRYPT_KEY = "key";

    private Integer currentSystemID;
    private IBSCertificate serverCertificate;
    private IBEPrivateKey serverPrivateKey;
    private Encoder base64;
    private RestTemplate restTemplate;

    private Map<Integer, String> systemIDs;
    private Map<Integer, IBEPublicParameter> systemParameters;

    private final static Logger logger = LoggerFactory.getLogger(KeyGenClient.class);

    @Autowired
    public KeyGenClient(RestTemplateBuilder restTemplateBuilder, @Qualifier("base64Encoder") Encoder base64) {
        this.restTemplate = requireNonNull(restTemplateBuilder).build();
        this.base64 = requireNonNull(base64);
        currentSystemID = -1;
        systemIDs = new ConcurrentHashMap<Integer, String>();
        systemParameters = new ConcurrentHashMap<Integer, IBEPublicParameter>();
    }

    public void init() {
        SimpleRESTResponse resp = restTemplate.getForObject(String.format("%s/system/%s/number", keyGenServereURL, systemIDStr), SimpleRESTResponse.class);
        if (resp.getResultCode() == 0) {
            currentSystemID = (Integer) resp.getPayload();
            logger.info("Connected to keygen server, default system: {} ", currentSystemID);
        } else {
            logger.error("Failed to connect to key generation server: {}, error code: {}, message: {}", keyGenServereURL, resp.getResultCode(), resp.getMessage());
        }
        @SuppressWarnings("unchecked")
        Map<String, String> allSystem = restTemplate.getForObject(String.format("%s/system/all", keyGenServereURL), Map.class);
        for (String key : allSystem.keySet()) {
            try {
                systemIDs.put(Integer.parseInt(key), allSystem.get(key));
            } catch (NumberFormatException e) {
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> allSystemParameters = restTemplate.getForObject(String.format("%s/system/allparam", keyGenServereURL), Map.class);
        for (String systemID : allSystemParameters.keySet()) {
            Map<String, String> base64EncodedParameter = allSystemParameters.get(systemID);
            IBEPublicParameter decodedParameter = new IBEPublicParameter();
            decodedParameter.setPairing(base64.decode(base64EncodedParameter.get("pairing")));
            decodedParameter.setParamG(base64.decode(base64EncodedParameter.get("paramG")));
            decodedParameter.setParamG1(base64.decode(base64EncodedParameter.get("paramG1")));
            decodedParameter.setParamH(base64.decode(base64EncodedParameter.get("paramH")));
            systemParameters.put(Integer.parseInt(systemID), decodedParameter);
        }
        prepareServerKeys();
        if (serverPrivateKey == null || serverCertificate == null)
            setupServerKeys();
    }

    public IBEPrivateKey serverPrivateKey() {
        if (serverPrivateKey != null)
            return serverPrivateKey;
        prepareServerKeys();
        return serverPrivateKey;
    }

    public IBSCertificate serverCertificate() {
        if (serverCertificate != null)
            return serverCertificate;
        prepareServerKeys();
        return serverCertificate;
    }

    private synchronized void prepareServerKeys() {
        File folder = new File(serverKeyLocation);
        if (checkFolerPermission(folder)) {
            Properties props = new Properties();
            InputStream key = null;
            try {
                key = new FileInputStream(new File(folder, SERVER_KEY_FILE));
                props.load(key);
            } catch (IOException e) {
                logger.error("Failed to read server private key", e);
                return;
            } finally {
                try {
                    if (key != null)
                        key.close();
                } catch (IOException e) {
                }
            }
            // both server key and its decryption key are stored in hex format
            String serverIdContent = props.getProperty(SERVER_KEY_FILE_CONTENT);
            String keyContent = props.getProperty(SERVER_KEY_FILE_CRYPT_KEY);
            byte[] raw = Hex.unhex(serverIdContent);
            ByteArrayInputStream in = new ByteArrayInputStream(raw);
            IBECapsule capsule = new IBECapsuleAESImpl();
            capsule.setKey(Hex.unhex(keyContent));
            IdentityDescription id = null;
            try {
                capsule.readExternal(in);
                id = (IdentityDescription) capsule.getDataAsObject();
            } catch (ClassNotFoundException | IOException e) {
                logger.error("Failed to decrypt server private key", e);
            } finally {
                capsule.close();
            }
            serverPrivateKey = id == null ? null : id.getPrivateKey();
            serverCertificate = id == null ? null : id.getCertificate();
            if (serverPrivateKey != null && serverCertificate != null)
                logger.info("Successfully loaded server private key");
        }
    }

    private boolean checkFolerPermission(File folder) {
        if (folder.isDirectory()) {
            final String folderPermission = "rwx------";
            if (SystemUtils.IS_OS_WINDOWS)
                return true;  // Windows does not support PosixFilePermissions
            if (folder.list().length == 0) {
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(folderPermission);
                try {
                    Files.setPosixFilePermissions(Paths.get(folder.getAbsolutePath()), permissions);
                    return true;
                } catch (IOException e) {
                    logger.error("Failed to set permission for new server private key directory", e);
                    return false;
                }
            }
            try {
                Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(Paths.get(folder.getAbsolutePath()));
                String currentPermission = PosixFilePermissions.toString(permissions);
                if (folderPermission.equalsIgnoreCase(currentPermission)) {
                    return true;
                } else {
                    logger.error("Incorrect permission settings for server key directory {}, expected {}, actual {}", folder.getAbsoluteFile(), folderPermission, currentPermission);
                    return false;
                }
            } catch (IOException e) {
                logger.error("Failed to read permission for existing server private key directory", e);
                return false;
            }
        }
        return false;
    }

    private File createNewKeyFile(File folder, String name, boolean overwrite) throws IOException {
        final String fildPermission = "rw-------";
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(fildPermission);
        File key = new File(folder, name);
        if (key.exists()) {
            if (overwrite) {
                logger.warn("Original key file {} will be overwritten", key.getAbsolutePath());
                if (!key.delete()) {
                    logger.error("Failed to overwrite existing key file");
                    return null;
                }
            } else {
                File backup = new File(folder, String.format("%s_%d", SERVER_KEY_FILE, System.currentTimeMillis()));
                if (key.renameTo(backup)) {
                    Files.setPosixFilePermissions(Paths.get(backup.getAbsolutePath()), permissions);
                    logger.warn("Creating server key backup file {} for {}", backup.getName(), key.getName());
                } else {
                    logger.error("Can't create server key backup file {} for {}", backup.getName(), key.getName());
                    return null;
                }
            }
        }
        key.createNewFile();
        Files.setPosixFilePermissions(Paths.get(key.getAbsolutePath()), permissions);
        return key;
    }

    public void setupServerKeys() {
        File folder = new File(serverKeyLocation);
        if (!folder.exists()) {
            folder.mkdirs();
            checkFolerPermission(folder);
        }
        byte[] sessionKey = randomKey(64);
        IBEPlainText plain = IBEPlainText.newIbePlainTextFormSignificantBytes(sessionKey);
        IBECipherText cipher = IBEEngine.encrypt(getKeyGenServerPublicParameter(currentSystemID), plain, getSystemIDStr(currentSystemID));
        IBECSR request = new IBECSR();
        request.setApplicationDate(new Date());
        request.setIdentityString(serverID);
        request.setIbeSystemId(currentSystemID);
        request.setPeriod(serverKeyValidPeriod);
        request.setPassword(cipher.toByteArray());
        ResponseEntity<SimpleRESTResponse> response = restTemplate.postForEntity(String.format("%s/singleid", keyGenServereURL), request, SimpleRESTResponse.class);
        if (response.hasBody()) {
            SimpleRESTResponse rest = response.getBody();
            if (rest.getResultCode() != 0) {
                logger.error("Failed to request server key: {}, deatil: {}", rest.getResultCode(), rest.getMessage());
                return;
            }
            Properties serverKey = new Properties();
            serverKey.setProperty(SERVER_KEY_FILE_CONTENT, response.getBody().getPayload().toString());
            serverKey.setProperty(SERVER_KEY_FILE_CRYPT_KEY, Hex.hex(sessionKey));
            OutputStream out = null;
            try {
                File key = createNewKeyFile(folder, SERVER_KEY_FILE, false);
                out = new FileOutputStream(key);
                serverKey.store(out, "server key");
                out.flush();
            } catch (IOException e) {
                logger.error("Error saving server key.", e);
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public byte[] encryptSessionKeyForSystem(byte[] idPwdBin, int ibeSystemId) {
        IBEPlainText plainText = IBEPlainText.newIbePlainTextFormSignificantBytes(idPwdBin);
        IBECipherText cipher = IBEEngine.encrypt(systemParameters.get(ibeSystemId), plainText , systemIDs.get(ibeSystemId));
        return cipher.toByteArray();
    }
    
    public byte[] encryptSessionKeyForSystem(byte[] bytes) {
        return encryptSessionKeyForSystem(bytes, currentSystemID);
    }

    public byte[] randomKey(int length) {
        byte[] bytes = new byte[length];
        Random rand = new SecureRandom();
        rand.nextBytes(bytes);
        return bytes;
    }

    public Map<Integer, String> getAvailableSystem() {
        return systemIDs;
    }

    public String getSystemIDStr(Integer systemID) {
        return systemIDs.get(systemID);
    }

    public IBEPublicParameter getKeyGenServerPublicParameter(Integer systemID) {
        return systemParameters.get(systemID);
    }

    public Integer getCurrentSystemID() {
        return currentSystemID;
    }

    public String getKeyGenServereURL() {
        return keyGenServereURL;
    }
}
