package in.demo.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import in.demo.blog.entity.Tag;


@Repository
public interface TagRepository  extends JpaRepository<Tag, Long>{


	Tag findByName(String t);
	
}
