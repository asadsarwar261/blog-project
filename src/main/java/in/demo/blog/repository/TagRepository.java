package in.demo.blog.repository;

import in.demo.blog.entity.Tag;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Tag findByName(String t);
	List<Tag> findByNameIn(Set<String> tagNames);
	List<Long> findIdByNameIn(Set<String> tagNames);
}
