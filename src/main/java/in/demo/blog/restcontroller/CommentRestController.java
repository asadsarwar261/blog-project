package in.demo.blog.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import in.demo.blog.entity.Comment;
import in.demo.blog.entity.User;
import in.demo.blog.service.CommentService;
import in.demo.blog.service.UserService;

@RestController
@RequestMapping("/comments")
public class CommentRestController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private UserService userService;

	@PostMapping("/submit")
	public ResponseEntity<String> submitComment(@RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("comment") String content, @RequestParam("postId") Long postId) {
		commentService.saveComment(name, email, content, postId);
		return ResponseEntity.status(HttpStatus.CREATED).body("Comment submitted successfully.");
	}

	@GetMapping("/{id}")
	public ResponseEntity<Comment> getCommentById(@PathVariable("id") Long commentId) {
		Comment comment = commentService.getCommentById(commentId);
		if (comment == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(comment);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<String> updateCommentHandler(@PathVariable("id") Long commentId,
			@AuthenticationPrincipal UserDetails userDetails, @RequestBody Comment updatedComment) {
		Comment existingComment = commentService.findCommentById(commentId);
		if (existingComment == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found.");
		}

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		if (!currentUserEmail.equals(existingComment.getEmail()) && !currentUser.getUserRole().equals("ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
		}

		commentService.updateComment(commentId, updatedComment);
		return ResponseEntity.ok("Comment updated successfully.");
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteCommentHandler(@PathVariable("id") Long commentId,
			@AuthenticationPrincipal UserDetails userDetails) {
		Comment comment = commentService.findCommentById(commentId);
		if (comment == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found.");
		}

		String currentUserEmail = userDetails.getUsername();
		User currentUser = userService.findByEmail(currentUserEmail);

		if (!currentUserEmail.equals(comment.getEmail()) && !currentUser.getUserRole().equals("ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
		}

		commentService.deleteComment(commentId);
		return ResponseEntity.ok("Comment deleted successfully.");
	}
}
