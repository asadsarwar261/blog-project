package in.demo.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import in.demo.blog.entity.User;
import in.demo.blog.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	public void saveUserInfo(User user) {

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
	}
	
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User findUserByAuthorName(String authorName) {
        return userRepository.findUserByName(authorName);
    }
	
}
