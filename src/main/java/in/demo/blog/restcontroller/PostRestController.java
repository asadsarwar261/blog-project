package in.demo.blog.restcontroller;

import in.demo.blog.entity.Post;
import in.demo.blog.entity.User;
import in.demo.blog.service.PostService;
import in.demo.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/posts")
public class PostRestController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @GetMapping("/home-page")
    public ResponseEntity<?> getHomePage(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortField", defaultValue = "publishedAt", required = false) String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection) {

        Page<Post> postList = postService.getListOfPost(pageNumber, pageSize, sortField, sortDirection);
        Set<User> authors = new HashSet<>();
        for (Post post : postService.findAll()) {
            authors.add(post.getAuthor());
        }
        
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<Post> viewPost(@PathVariable("id") Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(post);
    }

    @PostMapping("/add-post")
    public ResponseEntity<String> addPost(@RequestBody Post post, @AuthenticationPrincipal UserDetails userDetails) {
        String currentUserEmail = userDetails.getUsername();
        User currentUser = userService.findByEmail(currentUserEmail);
        post.setAuthor(currentUser);

        postService.savePost(post, "");  // Assuming empty tags for now
        return ResponseEntity.status(HttpStatus.CREATED).body("Post created successfully");
    }

    @PutMapping("/update-post/{id}")
    public ResponseEntity<String> updatePost(
            @PathVariable("id") Long postId,
            @RequestBody Post post,
            @AuthenticationPrincipal UserDetails userDetails) {

        String currentUserEmail = userDetails.getUsername();
        User currentUser = userService.findByEmail(currentUserEmail);
        Post existingPost = postService.getPostById(postId);

        if (existingPost != null && (existingPost.getAuthor().getEmail().equals(currentUser.getEmail())
                || currentUser.getUserRole().equals("ADMIN"))) {
            postService.updatePost(postId, post, currentUser.getName());
            return ResponseEntity.ok("Post updated successfully");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this post");
    }

    @DeleteMapping("/delete-post/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id") Long postId, @AuthenticationPrincipal UserDetails userDetails) {
        String currentUserEmail = userDetails.getUsername();
        User currentUser = userService.findByEmail(currentUserEmail);
        Post post = postService.getPostById(postId);

        if (post != null && (post.getAuthor().getEmail().equals(currentUser.getEmail())
                || currentUser.getUserRole().equals("ADMIN"))) {
            postService.deletePost(postId);
            return ResponseEntity.ok("Post deleted successfully");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this post");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPostFeature(
            @RequestParam(value = "query", required = false) String searchQuery,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        if (searchQuery == null || searchQuery.isEmpty()) {
            return ResponseEntity.badRequest().body("Search query cannot be empty");
        }

        Page<Post> searchResults = postService.findPage(searchQuery, pageNumber, pageSize);
        return ResponseEntity.ok(searchResults.getContent());
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterPosts(
            @RequestParam(value = "author", required = false) List<String> authors,
            @RequestParam(value = "publishedAt", required = false) String publishedDate,
            @RequestParam(value = "tags", required = false) List<String> tagIds,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

        Page<Post> filteredPosts = postService.filterPosts(authors, tagIds, publishedDate, pageNumber, pageSize);
        return ResponseEntity.ok(filteredPosts.getContent());
    }
}
