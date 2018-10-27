package hamaster.gradesign.keydist.web;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesign.keydist.daemon.KeyGenClient;
import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.IDRequestService;
import hamaster.gradesign.keygen.IBECSR;

@Controller
public class IDController {

    private final static String ID_MGMT = "idmgmt";
    private final static String NEW_ID = "newid";

    private IDRequestService idRequestService;
    private KeyGenClient client;

    @Autowired
    public IDController(IDRequestService idRequestService, KeyGenClient client) {
        this.idRequestService = requireNonNull(idRequestService);
        this.client = requireNonNull(client);
    }

    /**
     * Show a list of IDs for user, regardless of their status
     * @param pageStr page number starting from 1
     * @param amountStr amount per page, default 5
     * @param model Spring ui model
     * @param session http session
     * @return @see IDController.ID_MGMT
     */
    @GetMapping(value = "/id")
    public String idManagementPage(@RequestParam(name = "p", required = false) String pageStr,
            @RequestParam(name = "n", required = false) String amountStr,
            Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        int page;
        int amount;
        Object obj = session.getAttribute("user");
        if (obj == null) {
            return "redirect:/login";
        }
        try {
            page = Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            page = 1;
        }
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            amount = 5;
        }
        Map<String, ?> flashAttrs = redirectAttributes.getFlashAttributes();
        for (String key : flashAttrs.keySet()) {
            model.addAttribute(key, flashAttrs.get(key));
        }
        User user = (User) obj;
        List<IDRequest> ids = idRequestService.list(user, page - 1, amount);
        model.addAttribute("ids", ids);
        model.addAttribute("page", page);
        return ID_MGMT;
    }

    @GetMapping(value = "/idreq")
    public String newIDPage(Model model, HttpSession session) {
        Object obj = session.getAttribute("user");
        if (obj == null) {
            return "redirect:/login";
        }
        return backToCreateIDPageWithMessage(model, null);
    }

    @PostMapping(value = "/idreq")
    public String newID(@RequestParam(name = "email", required = true) String email,
            @RequestParam(name = "password", required = true) String password,
            @RequestParam(name = "confirmpassword", required = true) String confirmPassword,
            @RequestParam(name = "system", required = true) String systemIDStr,
            Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        int system;
        Object obj = session.getAttribute("user");
        if (obj == null) {
            redirectAttributes.addAttribute("messages", Arrays.asList("Please login first."));
            return "redirect:/login";
        }
        try {
            system = Integer.parseInt(systemIDStr);
        } catch (NumberFormatException e) {
            system = client.getCurrentSystemID();
        }
        IDRequest exist = idRequestService.getByIDString(email);
        if (exist != null && exist.getIbeSystemId().intValue() == system) {
            return backToCreateIDPageWithMessage(model, String.format("ID %s already exists.", email));
        }
        if (!password.equals(confirmPassword)) {
            return backToCreateIDPageWithMessage(model, "Encryption password mismatch.");
        }
        IDRequest newRequest = new IDRequest();
        newRequest.setApplicant((User) obj);
        newRequest.setIbeSystemId(system);
        newRequest.setIdentityString(email);
        newRequest.setStatus(IBECSR.APPLICATION_STARTED);
        newRequest.setPassword(Hex.hex(Hash.sha512(password)));
        newRequest.setPasswordToKeyGen(client.encryptSessionKeyForSystem(password.getBytes(), system));
        newRequest.setApplicationDate(new Date());
        idRequestService.save(newRequest);
        redirectAttributes.addFlashAttribute("message", "Request successfully submitted.");
        return "redirect:/id";
    }

    private String backToCreateIDPageWithMessage(Model model, String message) {
        if (message != null)
            model.addAttribute("message", message);
        model.addAttribute("systems", client.getAvailableSystem());
        return NEW_ID;
    }
}
