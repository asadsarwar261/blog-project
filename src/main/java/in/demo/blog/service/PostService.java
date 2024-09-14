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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

//		String userEmail = userDetails.getUsername();
//		User user = userService.findByEmail(userEmail);

		StringBuilder listOfTagInString = new StringBuilder();
		for (Tag tag : listOfTag) {
			listOfTagInString.append(tag.getName()).append(",");
		}

		String listOfTagString = listOfTagInString.toString();
		boolean hasNextPage = checkNextPage(pageNumber, pageSize);

//		model.addAttribute("user", user;
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

	public Page<Post> findPage(String searchQuery, int pageNumber, int pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Set<Post> setOfPosts = new HashSet<>();
		setOfPosts.addAll(postRepository.findByTitleContainingIgnoreCase(searchQuery, pageable));
		setOfPosts.addAll(postRepository.findByAuthorContainingIgnoreCase(searchQuery, pageable));
		setOfPosts.addAll(postRepository.findByTagsNameContainingIgnoreCase(searchQuery, pageable));
		setOfPosts.addAll(postRepository.findByContentContainingIgnoreCase(searchQuery, pageable));

		List<Post> combinedPostList = new ArrayList<>(setOfPosts);

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), combinedPostList.size());
		List<Post> paginatedPosts = combinedPostList.subList(start, end);

		return new PageImpl<>(paginatedPosts, pageable, combinedPostList.size());
//		return getPageRange(new ArrayList<>(setOfPosts), pageNumber);
//		return getPageRange(new ArrayList<>(setOfPosts), pageNumber);

//	    return new PageImpl<>(paginatedPosts, pageable, combinedPostList.size());
	}

//	private List<Post> getPageRange(List<Post> listPost, int pageNumber) {
//
//		int beginPage = pageNumber * 10;
//		int endPage = Math.min(beginPage + 10, listPost.size());
//		return listPost.subList(beginPage, endPage);
//	}

	public void searchOperation(String searchQuery, Model model, int pageNumber, int pageSize) {

		Page<Post> postPage = findPage(searchQuery, pageNumber, pageSize);
		model.addAttribute("postlist", postPage.getContent());
		model.addAttribute("currentPage", pageNumber);
		model.addAttribute("totalNumberOfPages", postPage.getTotalPages());
		model.addAttribute("pageSize", pageSize);
	}

	public int getTotalSearchPage(String searchQuery) {

		return (int) Math.ceil((double) findPage(searchQuery, 0, 10).getSize() / 10);
	}

	public Post updatePost(Long postId, Post updatedPost) {

//
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
			existingPost.setAuthor(updatedPost.getAuthor());
			existingPost.setUpdatedAt(LocalDate.now());

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

//	public List<Post> filterPosts(String author, String publishedDate, String tags) {
//        if (author != null && !author.isEmpty()) {
//            return postRepository.findByAuthorContainingIgnoreCase(author);
//        } else if (publishedDate != null && !publishedDate.isEmpty()) {
//            LocalDate date = LocalDate.parse(publishedDate);
//            return postRepository.findByPublishedAt(date);
//        } else if (tags != null && !tags.isEmpty()) {
//            return postRepository.findByTagsNameContainingIgnoreCase(tags);
//        }
//        return postRepository.findAll();
//	}
}