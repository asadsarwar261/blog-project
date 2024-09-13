package in.demo.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import in.demo.blog.entity.User;
import in.demo.blog.service.UserService;

@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/saveUser")
	public String saveNewUser(@ModelAttribute User user) {

		user.setUserRole("AUTHOR");
		userService.saveUserInfo(user);
		return "redirect:/login-page";
	}
	
	

}
