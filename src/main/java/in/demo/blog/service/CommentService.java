package in.demo.blog.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.demo.blog.entity.Comment;
import in.demo.blog.entity.Post;
import in.demo.blog.repository.CommentRepository;
import in.demo.blog.repository.PostRepository;

@Service
public class CommentService {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostRepository postRepository;

	public List<Comment> getAllComments() {
		return commentRepository.findAll();
	}

	public Comment getCommentById(Long id) {
		return commentRepository.findById(id).orElse(null);
	}

	public Post getPostById(Long id) {
		return postRepository.findById(id).orElse(null);
	}

	public void saveComment(String name, String email, String content, Long postId) {
		Post post = getPostById(postId);
		Comment comment = new Comment();
		comment.setName(name);
		comment.setEmail(email);
		comment.setContent(content);
		comment.setPost(post);
		comment.setCreatedAt(LocalDate.now());
		comment.setUpdatedAt(LocalDate.now());
		commentRepository.save(comment);
	}

	public Comment updateComment(Long id, Comment updatedComment) {
		Comment comment = getCommentById(id);
		if (comment != null) {
			comment.setContent(updatedComment.getContent());
			comment.setUpdatedAt(LocalDate.now());
			return commentRepository.save(comment);
		}
		return null;
	}

	public void deleteComment(Long id) {
		commentRepository.deleteById(id);
	}

	public Comment findCommentById(Long commentId) {
		return commentRepository.findById(commentId).orElse(null);
	}

	public void updateComment(String name, String email, String content, Long commentId) {
		Comment comment = commentRepository.findById(commentId).orElse(null);
		comment.setName(name);
		comment.setEmail(email);
		comment.setContent(content);
		comment.setId(commentId);
		comment.setUpdatedAt(LocalDate.now());
		commentRepository.save(comment);
	}

}