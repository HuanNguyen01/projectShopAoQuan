/**
 * @author Duong Van Luc 01/07/2000
 */
package fashion.mock.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fashion.mock.model.User;
import fashion.mock.model.DTO.TransactionHistoryDTO;
import fashion.mock.service.CustomerInformationService;
import fashion.mock.service.PurchaseHistoryService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/information")
public class PurchaseHistoryController {

    private final PurchaseHistoryService purchaseHistoryService;
    private final CustomerInformationService customerInformationService;

    public PurchaseHistoryController(PurchaseHistoryService purchaseHistoryService,
            CustomerInformationService customerInformationService) {
        this.purchaseHistoryService = purchaseHistoryService;
        this.customerInformationService = customerInformationService;
    }

    @GetMapping("purchase-history")
    public String viewPurchaseHistory(HttpSession session, @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size, Model model) {

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập thì chuyển về trang login
        }

        Long userId = sessionUser.getId(); // Lấy userId từ đối tượng User trong session
        User user = customerInformationService.getUserById(userId);
        model.addAttribute("user", user);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionHistoryDTO> transactionHistoriesPage = purchaseHistoryService
                .findAllTransactionHistoriesByUserId(userId, pageable);

        model.addAttribute("transactionHistoriesPage", transactionHistoriesPage);
        return "purchase-history";
    }

    @GetMapping("customer-information")
    public String viewCustomerInformation(HttpSession session, Model model) {
        return "customer-information";
    }
}
