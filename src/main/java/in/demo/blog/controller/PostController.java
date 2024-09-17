package in.demo.blog.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import in.demo.blog.entity.User;
import in.demo.blog.repository.TagRepository;
import in.demo.blog.service.PostService;
import in.demo.blog.service.UserService;

@Controller
public class PostController {

	@Autowired
	private PostService postService;

	@Autowired
	private UserService userService;

	@Autowired
	private TagRepository tagRepository;

	@GetMapping("/home-page")
	public String showHomePage(
			@RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
			@RequestParam(value = "sortField", defaultValue = "publishedAt", required = false) String sortField,
			@RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection,
			Model model) {

		postService.viewHomePage(pageNumber, pageSize, sortField, sortDirection, model);
		Set<User> listOfUsers = new HashSet<>();
		for (Post post : postService.findAll()) {
			listOfUsers.add(post.getAuthor());
		}
		model.addAttribute("authors", listOfUsers);
		model.addAttribute("tagList", tagRepository.findAll());

		return "home-page";
	}

	@GetMapping("/showLoginPage")
	public String getLoginPage() {
		return "login-page";
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
		post.setAuthor(currentUser);

		model.addAttribute("post", post);
		model.addAttribute("currentUserName", currentUser.getName());
		return "create-post-page";
	}

	@PostMapping("/save-post")
	public String savePost(@RequestParam("tagsList") String tagsList, @ModelAttribute("post") Post post,
			@RequestParam("authorName") String authorName, @AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);
		post.setAuthor(currentUser);
		postService.savePost(post, tagsList);
		return "redirect:/home-page";
	}

	@GetMapping("/edit-post/{id}")
	public String showUpdateForm(@PathVariable("id") Long postId, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		Post post = postService.getPostById(postId);
		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);
		String postAuthorName = post.getAuthor().getName();
		if (!postAuthorName.equals(currentUser.getName()) && !currentUser.getUserRole().equals("ADMIN")) {
			return "temp";
		}

		model.addAttribute("post", post);
//		model.addAttribute("tags", tags);

		System.out.println("hello1 ");

		return "edit-post-page";
	}

	@PostMapping("/update-post/{id}")
	public String updatePost(@PathVariable("id") Long postId, @ModelAttribute("post") Post post,
			@RequestParam("authorName") String authorName, @AuthenticationPrincipal UserDetails userDetails) {

		System.out.println("helo 2");
		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		Post currentPost = postService.getPostById(postId);

		if (currentPost != null && (currentPost.getAuthor().getEmail().equals(currentUser.getEmail())
				|| currentUser.getUserRole().equals("ADMIN"))) {

			postService.updatePost(postId, post, authorName);
			return "redirect:/post/" + postId;
		}

		return "temp";
	}

	@PostMapping("/delete-post/{id}")
	public String deletePost(@PathVariable("id") Long postId, @AuthenticationPrincipal UserDetails userDetails) {

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		Post post = postService.getPostById(postId);
		String postAuthorName = post.getAuthor().getName();
		if (!postAuthorName.equals(currentUser.getName()) && !currentUser.getUserRole().equals("ADMIN")) {
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

	@GetMapping("/filter")
	public String filterPosts(@RequestParam(value = "author", required = false) List<String> authors,
			@RequestParam(value = "publishedAt", required = false) String publishedDate,
			@RequestParam(value = "tags", required = false) List<String> tagIds,
			@RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize, Model model) {

		Page<Post> filteredPosts = postService.filterPosts(authors, tagIds, publishedDate, pageNumber, pageSize);
		Set<User> listOfUsers = new HashSet<>();
		for (Post post : postService.findAll()) {
			listOfUsers.add(post.getAuthor());
		}
		model.addAttribute("postlist", filteredPosts.getContent());
		model.addAttribute("authors", listOfUsers);
		model.addAttribute("tagList", tagRepository.findAll());

		return "home-page";
	}
}
