package in.demo.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.demo.blog.entity.Post;
import in.demo.blog.entity.Tag;
import in.demo.blog.entity.User;
import in.demo.blog.service.PostService;
import in.demo.blog.service.UserService;

@Controller
public class PostController {

	@Autowired
	private PostService postService;

	@Autowired
	private UserService userService;

	@GetMapping("/home-page")
	public String showHomePage(
			@RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
			@RequestParam(value = "sortField", defaultValue = "publishedAt", required = false) String sortField,
			@RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection,
			Model model) {

		postService.viewHomePage(pageNumber, pageSize, sortField, sortDirection, model);

		return "home-page";
	}

	@GetMapping("/showLoginPage")
	public String getLoginPage() {
		return "login-page";
	}

//	@PostMapping("/login-page")
//	public String handleLoginPage() {
//		return "login-page";
//	}

	@GetMapping("/register-page")
	public String getRegisterPage() {
		return "register-page";
	}

	@PostMapping("/register-page")
	public String handleRegisterPage() {
		return "register-page";
	}

	@GetMapping("/post/{id}")
	public String viewPost(@PathVariable("id") Long postId, Model model) {
		Post post = postService.getPostById(postId);
		model.addAttribute("post", post);
		return "view-post-page";
	}

	@GetMapping("/add-post")
	public String addPost(Model model, @AuthenticationPrincipal UserDetails userDetails) {

		Post post = new Post();

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		post.setAuthor(currentUser.getName());
		model.addAttribute("post", post);
		return "create-post-page";
	}

	@PostMapping("/save-post")
	public String savePost(@RequestParam("tagsList") String tagsList, @ModelAttribute("post") Post post,
			@AuthenticationPrincipal UserDetails userDetails, Model model) {

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		post.setAuthor(currentUser.getName());

		postService.savePost(post, tagsList);
		return "redirect:/home-page";
	}

	@GetMapping("/edit-post/{id}")
	public String showUpdateForm(@PathVariable("id") Long postId, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		Post post = postService.getPostById(postId);
		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		if (!post.getAuthor().equals(currentUser.getName()) && !currentUser.getUserRole().equals("ADMIN")) {
//			redirect to error page
			return "temp";
		}

		model.addAttribute("post", post);
//		model.addAttribute("tags", tags);

		return "edit-post-page";
	}

	@PostMapping("/update-post/{id}")
	public String updatePost(@PathVariable("id") Long postId, @ModelAttribute("post") Post post,
			@AuthenticationPrincipal UserDetails userDetails) {
//			@ModelAttribute Tag tags,
//		String 

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		Post currentPost = postService.getPostById(postId);
		if (!currentPost.getAuthor().equals(currentUser.getName()) && !currentUser.getUserRole().equals("ADMIN")) {
			return "temp";
		}

//		postService.updatePost(postId, post,tags);
		postService.updatePost(postId, post);
		return "redirect:/post/" + postId;
	}

	@PostMapping("/delete-post/{id}")
	public String deletePost(@PathVariable("id") Long postId, @AuthenticationPrincipal UserDetails userDetails) {

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		Post post = postService.getPostById(postId);

		if (!post.getAuthor().equals(currentUser.getName()) && !currentUser.getUserRole().equals("ADMIN")) {
			return "temp";
		}

		postService.deletePost(postId);
		return "redirect:/home-page";
	}

	@GetMapping("/search")
	public String searchPostFeature(@RequestParam(value = "query", required = false) String searchQuery, Model model,
			@RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

		if (searchQuery == null || searchQuery.isEmpty()) {
			return "redirect:/home-page";
		}

		postService.searchOperation(searchQuery, model, pageNumber, pageSize);
		return "home-page";
	}

//    @GetMapping("/filter")
//    public String filterPosts(@RequestParam(value = "author", required = false) String author,
//                              @RequestParam(value = "publishedDate", required = false) String publishedDate,
//                              @RequestParam(value = "tags", required = false) String tags,
//                              Model model) {
//        List<Post> filteredPosts = postService.filterPosts(author, publishedDate, tags);
//        model.addAttribute("postlist", filteredPosts);
//        model.addAttribute("author", author);
//        model.addAttribute("publishedDate", publishedDate);
//        model.addAttribute("tags", tags);
//        return "post-list";
//    }

}