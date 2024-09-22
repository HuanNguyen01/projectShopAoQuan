/**
 * Trần Thảo
 * Author: Ngô Văn Quốc Thắng 11/05/1996
 */
package fashion.mock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	/**
	 * Author: Ngô Văn Quốc Thắng 11/05/1996
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Trần Thảo
	 */
	// Định nghĩa bean SecurityFilterChain
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()) // Tắt CSRF để đơn giản hóa (chỉ nên làm điều này khi cần)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/home/**", "/css/**", "/js/**", "/login/**",
								"/forgot-password", "/verify-code", "/reset-password", "/products/**", "/css/**",
								"/shopping-cart/**", "/shop/**", "/js/**", "/images/**", "/shop/**", "/categories/**","/information/**",
								"/users/**", "/discounts/**", "/checkout/**","/information/**","/discounts/**","/orderDetail/**", "/register/**")
						.permitAll() // Cho phép truy cập không cần xác thực
//						.requestMatchers("/admin").hasAuthority("ADMIN") // Chỉ ADMIN mới được truy cập
						.anyRequest().authenticated() // Các yêu cầu khác phải xác thực
				).formLogin(form -> form.loginPage("/login/loginform") // Trang đăng nhập tùy chỉnh
						.defaultSuccessUrl("/home", true) // Chuyển hướng sau khi đăng nhập thành công
						.permitAll())
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/home") // Chuyển hướng sau khi đăng
																						// xuất
						.permitAll());
		return http.build();

	}

}
