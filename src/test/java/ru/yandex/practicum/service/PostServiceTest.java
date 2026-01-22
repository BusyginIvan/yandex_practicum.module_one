package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.exception.not_found.PostNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PostServiceTest extends AbstractServiceTest {

    @Autowired PostService postService;

    // ========================= getById =========================

    @Test
    void getPost() {
        long id = 1L;
        PostEntity entity = new PostEntity(id, "t", "txt", 7);

        when(postRepository.findById(id)).thenReturn(Optional.of(entity));
        when(commentRepository.countByPostId(id)).thenReturn(3);
        when(tagRepository.findTagsByPostId(id)).thenReturn(List.of("tag1", "tag2"));

        Post post = postService.getPost(id);

        assertEquals(id, post.id());
        assertEquals("t", post.title());
        assertEquals("txt", post.text());
        assertEquals(7, post.likesCount());
        assertEquals(3, post.commentsCount());
        assertEquals(List.of("tag1", "tag2"), post.tags());

        verify(postRepository).findById(id);
        verify(commentRepository).countByPostId(id);
        verify(tagRepository).findTagsByPostId(id);
        verifyNoInteractions(postImageStorage);
    }

    @Test
    void getPost_notFound() {
        when(postRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getPost(42L));

        verify(postRepository).findById(42L);
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(commentRepository, tagRepository, postImageStorage);
    }

    // ========================= create =========================

    @Test
    void create() {
        long newId = 10L;

        when(postRepository.insert("title", "text")).thenReturn(newId);

        PostEntity entity = new PostEntity(newId, "title", "text", 0);
        when(postRepository.findById(newId)).thenReturn(Optional.of(entity));
        when(commentRepository.countByPostId(newId)).thenReturn(0);
        when(tagRepository.findTagsByPostId(newId)).thenReturn(List.of("a", "b"));

        Post created = postService.create("title", "text", List.of("a", "b"));

        assertEquals(newId, created.id());
        assertEquals("title", created.title());
        assertEquals("text", created.text());
        assertEquals(0, created.likesCount());
        assertEquals(0, created.commentsCount());
        assertEquals(List.of("a", "b"), created.tags());

        verify(postRepository).insert("title", "text");
        verify(tagRepository).replaceTags(newId, List.of("a", "b"));
        verifyNoInteractions(postImageStorage);
    }

    // ========================= update =========================

    @Test
    void update() {
        long id = 5L;

        PostEntity entity = new PostEntity(id, "new", "newText", 2);

        doNothing().when(postRepository).update(id, "new", "newText");
        when(postRepository.findById(id)).thenReturn(Optional.of(entity));
        when(commentRepository.countByPostId(id)).thenReturn(1);
        when(tagRepository.findTagsByPostId(id)).thenReturn(List.of("x"));

        Post updated = postService.update(id, "new", "newText", List.of("x"));

        assertEquals(id, updated.id());
        assertEquals("new", updated.title());
        assertEquals("newText", updated.text());
        assertEquals(2, updated.likesCount());
        assertEquals(1, updated.commentsCount());
        assertEquals(List.of("x"), updated.tags());

        verify(postRepository).update(id, "new", "newText");
        verify(tagRepository).replaceTags(id, List.of("x"));
        verifyNoInteractions(postImageStorage);
    }

    // ========================= delete =========================

    @Test
    void delete() {
        postService.delete(99L);

        verify(postRepository).deleteById(99L);
        verify(postImageStorage).delete(99L);
        verifyNoInteractions(commentRepository, tagRepository);
    }

    // ========================= search =========================

    @Test
    void search() {
        when(postRepository.countBySearch("hello", List.of("t"))).thenReturn(6);

        PostEntity e1 = new PostEntity(101L, "h1", "body1", 1);
        PostEntity e2 = new PostEntity(100L, "h2", "body2", 0);

        when(postRepository.searchPage("hello", List.of("t"), 5, 5)).thenReturn(List.of(e1, e2));

        when(commentRepository.countByPostIds(List.of(101L, 100L))).thenReturn(Map.of(101L, 2));
        when(tagRepository.findTagsByPostIds(List.of(101L, 100L))).thenReturn(Map.of(
            101L, List.of("t"),
            100L, List.of("t", "u")
        ));

        PostPage page = postService.search("hello #t", 2, 5);

        assertEquals(2, page.pageNumber());
        assertEquals(5, page.pageSize());
        assertEquals(2, page.lastPage());
        assertTrue(page.hasPrev());
        assertFalse(page.hasNext());

        assertEquals(2, page.posts().size());

        Post p1 = page.posts().get(0);
        assertEquals(101L, p1.id());
        assertEquals(2, p1.commentsCount());
        assertEquals(List.of("t"), p1.tags());

        Post p2 = page.posts().get(1);
        assertEquals(100L, p2.id());
        assertEquals(0, p2.commentsCount());
        assertEquals(List.of("t", "u"), p2.tags());

        verify(postRepository).countBySearch("hello", List.of("t"));
        verify(postRepository).searchPage("hello", List.of("t"), 5, 5);
        verify(commentRepository).countByPostIds(List.of(101L, 100L));
        verify(tagRepository).findTagsByPostIds(List.of(101L, 100L));
        verifyNoInteractions(postImageStorage);
    }

    @Test
    void search_pageNumberGreaterThanLastPage() {
        when(postRepository.countBySearch("", List.of())).thenReturn(0); // lastPage=1

        when(postRepository.searchPage("", List.of(), 0, 10)).thenReturn(List.of());

        PostPage page = postService.search("", 999, 10);

        assertEquals(1, page.pageNumber());
        assertEquals(1, page.lastPage());
        assertFalse(page.hasPrev());
        assertFalse(page.hasNext());

        verify(postRepository).countBySearch("", List.of());
        verify(postRepository).searchPage("", List.of(), 0, 10);
        verify(commentRepository).countByPostIds(List.of());
        verify(tagRepository).findTagsByPostIds(List.of());
    }

    // ========================= incrementLikes =========================

    @Test
    void incrementLikes() {
        when(postRepository.incrementLikes(7L)).thenReturn(123);

        int v = postService.incrementLikes(7L);

        assertEquals(123, v);
        verify(postRepository).incrementLikes(7L);
        verifyNoInteractions(commentRepository, tagRepository, postImageStorage);
    }
}
