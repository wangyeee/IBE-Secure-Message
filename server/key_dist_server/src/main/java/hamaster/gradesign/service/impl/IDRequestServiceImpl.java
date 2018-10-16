package hamaster.gradesign.service.impl;

import static java.util.Objects.requireNonNull;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.IBECSR;
import hamaster.gradesign.dao.IDRequestDAO;
import hamaster.gradesign.entity.IDRequest;
import hamaster.gradesign.entity.User;
import hamaster.gradesign.ibe.util.Hex;
import hamaster.gradesign.service.IDRequestService;

public class IDRequestServiceImpl implements IDRequestService {

    private IDRequestDAO idRequestRepo;
    private DataSource dataSource;

    @Value("${ibe.key.dist.batch_size:100}")
    private int batchSize;

    @Autowired
    public IDRequestServiceImpl(IDRequestDAO idRequestRepo, DataSource dataSource) {
        this.idRequestRepo = requireNonNull(idRequestRepo);
        this.dataSource = requireNonNull(dataSource);
    }

    @Override
    public IDRequest getByOwner(User owner, String idString) {
        Optional<IDRequest> request = idRequestRepo.findByOwnerForID(requireNonNull(owner), requireNonNull(idString));
        if (request.isPresent())
            return request.get();
        return null;
    }

    @Override
    public List<IDRequest> listNewRequests(int amount) {
        return idRequestRepo.findAllByStatus(IBECSR.APPLICATION_NOT_VERIFIED, PageRequest.of(0, amount));
    }

    @Override
    public List<IDRequest> list(User owner, int page, int amount, int status) {
        return idRequestRepo.findAllByUserAndStatus(owner, status, PageRequest.of(page, amount));
    }

    @Override
    public List<IDRequest> listUnhandledRequests(int amount) {
        return idRequestRepo.findAllByStatus(IBECSR.APPLICATION_STARTED, PageRequest.of(0, amount));
    }

    @Override
    public void requestHandled(Map<String, Integer> results) {
        // SHA2 function is used
        BatchSqlUpdate sql = new BatchSqlUpdate(dataSource, "UPDATE IBE_ID_REQUEST SET PASSWORD=SHA2(PASSWORD, 512), APPLICATION_STATUS=? WHERE IDENTITY_STRING=? AND APPLICATION_STATUS<2");
        sql.setBatchSize(batchSize > results.size() ? results.size() : batchSize);
        sql.declareParameter(new SqlParameter(Types.INTEGER));
        sql.declareParameter(new SqlParameter(Types.VARCHAR));
        for (String id : results.keySet()) {
            sql.update(id, results.get(id));
        }
    }

    @Override
    public long count(User owner) {
        return idRequestRepo.countByOwner(requireNonNull(owner));
    }

    @Override
    public int doesIdBelongToUser(String id, User user, String idPassword) {
        Optional<IDRequest> request = idRequestRepo.findByOwnerForID(user, id);
        if (request.isPresent() == false) {
            return 2;  // the id doesn't belong to that user
        }
        String hash = request.get().getPassword();
        String exptHash = Hex.hex(Hash.sha512(idPassword));
        if (!exptHash.equalsIgnoreCase(hash))
            return 1;  // incorrect password
        return 0;
    }

    @Override
    public int doesIdRequestExist(String id) {
        IDRequest e = new IDRequest();
        e.setIdentityString(id);
        e.setStatus(IBECSR.APPLICATION_NOT_VERIFIED);
        Optional<IDRequest> request = idRequestRepo.findOne(Example.of(e));
        if (request.isPresent()) {
            return request.get().getStatus() + 1;
        }
        return 0;
    }

    @Override
    public void save(IDRequest request) {
        idRequestRepo.save(request);
    }
}
