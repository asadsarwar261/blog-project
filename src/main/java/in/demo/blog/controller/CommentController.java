package in.demo.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.demo.blog.entity.Comment;
import in.demo.blog.repository.CommentRepository;
import in.demo.blog.service.CommentService;

@Controller
public class CommentController {

	@Autowired
	private CommentService commentService;

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

	@PostMapping("/update-comment")
	public String updateComment(@RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("content") String content, @RequestParam("commentId") Long commentId,
			@RequestParam("postId") Long postId) {
		commentService.updateComment(name, email, content, commentId);
		return "redirect:/post/" + postId;
	}

	@PostMapping("/delete-comment/{id}")
	public String deleteCommentHandler(@PathVariable("id") Long commentId, @RequestParam("postId") Long postId) {
		commentRepository.deleteById(commentId);
		return "redirect:/post/" + postId;
	}

	@GetMapping("/update-comment/{id}")
	public String updateCommentHandler(@PathVariable("id") Long commentId, @RequestParam("postId") Long postId,
			Model model) {
		model.addAttribute("postId", postId);
		Comment comment = commentService.findCommentById(commentId);
		model.addAttribute("commentName", comment);
		return "update-comment-form";
	}
}