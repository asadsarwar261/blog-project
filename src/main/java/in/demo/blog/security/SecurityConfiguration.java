package in.demo.blog.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	
    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
        jdbcUserDetailsManager.setUsersByUsernameQuery("SELECT email, password, true AS enabled FROM users WHERE email = ?");
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery("SELECT email, user_role FROM users WHERE email = ?");
        return jdbcUserDetailsManager;
    }
    
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		// Define access rules
				.authorizeHttpRequests(configurer -> configurer
						.requestMatchers("/","/post/**", "/css/**", "/register-page", "/view-post-page", "/search",
								"/filter", "/sort", "/comment-post/**", "/submit-comment","/home-page","/saveUser")
						.permitAll()
						.requestMatchers("/add-post", "/edit-post/**", "/delete-post/**","/save-post/**","/update-post/**").hasAnyAuthority("AUTHOR", "ADMIN")
//						.requestMatchers("/add-post", "/edit-post/**", "/delete-post/**","/save-post/**","/update-post/**").hasAuthority("ADMIN")
						.requestMatchers( "/delete-comment/**", "/update-comment/**")
						.authenticated()
						.anyRequest().authenticated() 
				)
				.formLogin(form -> form
						.loginPage("/showLoginPage")
						.loginProcessingUrl("/authenticateUser")
						.defaultSuccessUrl("/home-page", true)
						.permitAll())
				.logout(logout -> logout
						.permitAll());
		
		return http.build();
	}
}
