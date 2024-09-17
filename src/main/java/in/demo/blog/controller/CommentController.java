package in.demo.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.demo.blog.entity.Comment;
import in.demo.blog.entity.User;
import in.demo.blog.repository.CommentRepository;
import in.demo.blog.service.CommentService;
import in.demo.blog.service.UserService;

@Controller
public class CommentController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommentRepository commentRepository;

	@GetMapping("/comment-post/{id}")
	public String getCommentForm(@PathVariable("id") Long postId, Model model) {
		model.addAttribute("postId", postId);
		return "comment-form";
	}

	@PostMapping("/submit-comment")
	public String submitComment(@RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("comment") String content, @RequestParam("postId") Long postId) {
		commentService.saveComment(name, email, content, postId);
		return "redirect:/post/" + postId;
	}

	@GetMapping("/update-comment/{id}")
	public String updateCommentHandler(@PathVariable("id") Long commentId, @RequestParam("postId") Long postId,
			@AuthenticationPrincipal UserDetails userDetails, Model model) {

		Comment comment = commentService.findCommentById(commentId);
		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		if (!currentUserEmail.equals(comment.getEmail()) && !currentUser.getUserRole().equals("ADMIN")) {
			return "temp";
		}

		model.addAttribute("postId", postId);
		model.addAttribute("commentName", comment);
		return "update-comment-form";
	}

	@PostMapping("/update-comment")
	public String updateComment(@RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("content") String content, @RequestParam("commentId") Long commentId,
			@RequestParam("postId") Long postId) {

		commentService.updateComment(name, email, content, commentId);
		return "redirect:/post/" + postId;
	}

	@PostMapping("/delete-comment/{id}")
	public String deleteCommentHandler(@PathVariable("id") Long commentId, @RequestParam("postId") Long postId,
			@AuthenticationPrincipal UserDetails userDetails) {

		Comment comment = commentService.findCommentById(commentId);
		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		if (!currentUserEmail.equals(comment.getEmail()) && !currentUser.getUserRole().equals("ADMIN")) {
			return "temp";
		}

		commentRepository.deleteById(commentId);
		return "redirect:/post/" + postId;
	}

}