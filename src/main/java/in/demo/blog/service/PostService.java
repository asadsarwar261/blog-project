package in.demo.blog.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import in.demo.blog.entity.Post;
import in.demo.blog.entity.Tag;
import in.demo.blog.entity.User;
import in.demo.blog.repository.PostRepository;
import in.demo.blog.repository.TagRepository;

@Service
public class PostService {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private UserService userService;

	public void viewHomePage(Integer pageNumber, Integer pageSize, String sortField, String sortDirection,
			Model model) {

		Page<Post> postList = getListOfPost(pageNumber, pageSize, sortField, sortDirection);
		List<Tag> listOfTag = tagRepository.findAll();

		StringBuilder listOfTagInString = new StringBuilder();
		for (Tag tag : listOfTag) {
			listOfTagInString.append(tag.getName()).append(",");
		}

		listOfTagInString.toString();
		boolean hasNextPage = checkNextPage(pageNumber, pageSize);

		model.addAttribute("postlist", postList);
		model.addAttribute("pageNumber", pageNumber);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDirection", sortDirection);
		model.addAttribute("tagList", listOfTag);
	}

	public Page<Post> getListOfPost(Integer pageNumber, Integer pageSize, String sortField, String sortDirection) {

		Sort sortBy = Sort.by(sortField).descending();
		if (sortDirection.equalsIgnoreCase("asc")) {
			sortBy = Sort.by(sortField).ascending();
		}

		Pageable pageable = PageRequest.of(pageNumber, pageSize, sortBy);
		Page<Post> pagePost = postRepository.findAll(pageable);
		return pagePost;
	}

	public boolean checkNextPage(Integer pageNumber, Integer pageSize) {

		Long totalNumberOfPosts = postRepository.count();
		Integer totalNumberOfPages = (int) Math.ceil(totalNumberOfPosts / pageSize);

		if (totalNumberOfPages > pageNumber) {
			return true;
		}
		return false;
	}

	public Post getPostById(Long postId) {

		Post post = postRepository.findById(postId).orElse(null);
		return post;
	}

	public void savePost(Post post, String tags) {

		Set<Tag> listOfTags = parseTags(tags);

		post.setCreatedAt(LocalDate.now());
		post.setPublishedAt(LocalDate.now());
		post.setUpdatedAt(LocalDate.now());
		post.setTags(listOfTags);
		postRepository.save(post);
	}

	public void deletePost(Long postId) {

		postRepository.deleteById(postId);
	}

	public void searchOperation(String searchQuery, Model model, int pageNumber, int pageSize) {

		Page<Post> postPage = findPage(searchQuery, pageNumber, pageSize);
		model.addAttribute("postlist", postPage.getContent());
		model.addAttribute("currentPage", pageNumber);
		model.addAttribute("totalNumberOfPages", postPage.getTotalPages());
		model.addAttribute("pageSize", pageSize);
	}

	public Page<Post> findPage(String searchQuery, int pageNumber, int pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Set<Post> setOfPosts = new HashSet<>();
//		here is an issue with the custom method naming
		setOfPosts.addAll(postRepository.findByTitleContainingIgnoreCase(searchQuery, pageable));
		setOfPosts.addAll(postRepository.findByAuthorNameContainingIgnoreCase(searchQuery, pageable));
		setOfPosts.addAll(postRepository.findByTagsNameContainingIgnoreCase(searchQuery, pageable));
		setOfPosts.addAll(postRepository.findByContentContainingIgnoreCase(searchQuery, pageable));

		List<Post> combinedPostList = new ArrayList<>(setOfPosts);

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), combinedPostList.size());
		List<Post> paginatedPosts = combinedPostList.subList(start, end);

		return new PageImpl<>(paginatedPosts, pageable, combinedPostList.size());
	}

	public Page<Post> filterPosts(List<String> authors, List<String> tagIds, String date, Integer pageNumber,
			Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		LocalDate publishedDate = null;
		if (date != null && !date.trim().isEmpty()) {
			publishedDate = LocalDate.parse(date);
		}

		if (tagIds != null && !tagIds.isEmpty() && publishedDate != null && authors != null && !authors.isEmpty()) {
			return postRepository.findByTagsNameInAndPublishedAtAndAuthorNameIn(tagIds, publishedDate, authors,
					pageable);
		} else if (tagIds != null && !tagIds.isEmpty()) {
			return postRepository.findByTagsNameIn(tagIds, pageable);
		} else if (publishedDate != null) {
			return postRepository.findByPublishedAt(publishedDate, pageable);
		} else if (authors != null && !authors.isEmpty()) {
			return postRepository.findByAuthorNameIn(authors, pageable);
		} else {
			return postRepository.findAll(pageable);
		}
	}

	public int getTotalSearchPage(String searchQuery) {
		return (int) Math.ceil((double) findPage(searchQuery, 0, 10).getSize() / 10);
	}

	public Post updatePost(Long postId, Post updatedPost, String authorName) {
//		StringBuilder listOfTagInString = new StringBuilder();
//		for (Tag tag : tags) {
//			listOfTagInString.append(tag.getName()).append(",");
//		}
//
//		String listOfTagString = listOfTagInString.toString();

		Post existingPost = getPostById(postId);

		if (existingPost != null) {
			existingPost.setTitle(updatedPost.getTitle());
			existingPost.setExcerpt(updatedPost.getExcerpt());
			existingPost.setContent(updatedPost.getContent());
			existingPost.setUpdatedAt(LocalDate.now());

			if (authorName != null && !authorName.isEmpty()) {
				User author = userService.findUserByAuthorName(authorName);
				if (author != null) {
					existingPost.setAuthor(author);
				}
			}

			// Handle tags
//            Set<Tag> listOfTags = parseTags(tags);
//            existingPost.setTags(listOfTags);

			return postRepository.save(existingPost);
		}
		return null;
	}

	public Set<Tag> parseTags(String tags) {

		Set<Tag> tagList = new HashSet<>();
		String[] tagArray = tags.split(",");
		for (String t : tagArray) {
			Tag findTag = tagRepository.findByName(t.trim());

			if (findTag == null) {
				Tag tag = new Tag();
				tag.setName(t.trim());
				tag.setCreatedAt(LocalDate.now());
				tag.setUpdatedAt(LocalDate.now());
				tagRepository.save(tag);
				findTag = tagRepository.findByName(t.trim());
			}
			tagList.add(findTag);
		}
		return tagList;
	}

	public List<Post> findAll() {
		return postRepository.findAll();
	}
}