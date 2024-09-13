package in.demo.blog.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.demo.blog.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

	List<Post> findByTitleContainingIgnoreCase(String title,Pageable pageable);
	List<Post> findByTagsNameContainingIgnoreCase(String tag,Pageable pageable);
	List<Post> findByAuthorContainingIgnoreCase(String author,Pageable pagaPageable);
	List<Post> findByContentContainingIgnoreCase(String searchQuery, Pageable pageable);
	List<Post> findByPublishedAt(LocalDate publishedAt);
}
