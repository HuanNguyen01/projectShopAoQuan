/**
 * @author Tran Thien Thanh 09/04/1996
 */
package fashion.mock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fashion.mock.model.Payment;

public interface CheckoutRepository extends JpaRepository<Payment, Long> {

	Payment findByPaymentMethod(String paymentMethod);
	
	List<Payment> findByStatus(String status);

}
