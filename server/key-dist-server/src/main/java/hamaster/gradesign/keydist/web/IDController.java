package hamaster.gradesign.keydist.web;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.IDRequestService;

@Controller
public class IDController {

    private final static String ID_MGMT = "idmgmt";

    private IDRequestService idRequestService;

    @Autowired
    public IDController(IDRequestService idRequestService) {
        this.idRequestService = requireNonNull(idRequestService);
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
            Model model, HttpSession session) {
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
        User user = (User) obj;
        List<IDRequest> ids = idRequestService.list(user, page - 1, amount);
        model.addAttribute("ids", ids);
        model.addAttribute("page", page);
        return ID_MGMT;
    }
}
