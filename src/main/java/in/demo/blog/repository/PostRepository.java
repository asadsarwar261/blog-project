package in.demo.blog.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.demo.blog.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

	List<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

	List<Post> findByTagsNameContainingIgnoreCase(String tag, Pageable pageable);

	List<Post> findByAuthorNameContainingIgnoreCase(String name, Pageable pageable);

	List<Post> findByContentContainingIgnoreCase(String searchQuery, Pageable pageable);

	List<Post> findByTags(Set<Long> tags);

	List<Post> findByAuthorInAndTagsIdIn(Set<String> authorNames, Set<Long> tagIds, Pageable pageable);

	List<Post> findByAuthorIn(Set<String> authorNames, Pageable pageable);

	List<Post> findByTagsIdIn(Set<Long> tagIds, Pageable pageable);

	List<Post> findByAuthorInAndTagsIdIn(Set<String> authorNames, Set<Long> tagIds);

	Page<Post> findByTagsNameInAndPublishedAtAndAuthorNameIn(List<String> tagId, LocalDate publishedDate,
			List<String> author, Pageable pageable);

	Page<Post> findByTagsNameIn(List<String> tagId, Pageable pageable);

	Page<Post> findByPublishedAt(LocalDate publishedDate, Pageable pageable);

	Page<Post> findByAuthorNameIn(List<String> author, Pageable pageable);

}
