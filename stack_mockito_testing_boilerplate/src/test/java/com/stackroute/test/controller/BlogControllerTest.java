package com.stackroute.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackroute.controller.BlogController;
import com.stackroute.domain.Blog;
import com.stackroute.exception.BlogAlreadyExistsException;
import com.stackroute.exception.BlogNotFoundException;
import com.stackroute.service.BlogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlogController.class)
public class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock BlogService layer
    @InjectMocks
    private BlogController blogController;

    @MockBean
    private BlogService blogService;
    @Autowired
    WebApplicationContext webApplicationContext;

    // Inject BlogService into BlogController

    private Blog blog;
    private List<Blog> blogList;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(blogController).build();
        blog = new Blog();
        blog.setBlogId(1);
        blog.setBlogTitle("DemoBlog");
        blog.setAuthorName("Imneet");
        blog.setBlogContent("SampleBlogforTesting");
        blogList = new ArrayList<>();
        blogList.add(blog);
    }

    @AfterEach
    public void tearDown() {
        blog = null;
    }

    /*
     * Test - POST mapping "/api/v1/blog" to save a blog, by mocking service class
     */
    @Test
    public void givenBlogToSaveThenShouldReturnSavedBlog() throws Exception {
        when(blogService.saveBlog(any())).thenReturn(blog);
        mockMvc.perform(post("/api/v1/blog")
                .contentType(APPLICATION_JSON)
                .content(asJsonString(blog)))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print());
        verify(blogService).saveBlog(any());

    }

    /*
     * Test - GET mapping "/api/v1/blogs" to get all blogs, by mocking service class
     */
    @Test
    public void givenGetAllBlogsThenShouldReturnListOfAllBlogs() throws Exception {
        when(blogService.getAllBlogs()).thenReturn(blogList);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/blogs")
                        .contentType(APPLICATION_JSON).content(asJsonString(blog)))
                .andDo(MockMvcResultHandlers.print());
        verify(blogService).getAllBlogs();
        verify(blogService, times(1)).getAllBlogs();

    }

    /*
     * Test - GET mapping "/api/v1/blog/1" to get a blog by id, by mocking service class
     */
    @Test
    public void givenBlogIdThenShouldReturnRespectiveBlog() throws Exception {
        when(blogService.getBlogById(1)).thenReturn(blog);

        // Act & Assert: Blog found
        mockMvc.perform(get("/api/v1/blog/1")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.blogTitle").value(blog.getBlogTitle()))
                .andExpect(jsonPath("$.authorName").value(blog.getAuthorName()))
                .andExpect(jsonPath("$.blogContent").value(blog.getBlogContent()));
    }

    /*
     * Test - DELETE mapping "/api/v1/blog/1" to delete a blog by id, by mocking service class
     */
    @Test
    public void givenBlogIdToDeleteThenShouldNotReturnDeletedBlog() throws Exception {
        when(blogService.deleteBlog(blog.getBlogId())).thenReturn(blog);
        mockMvc.perform(delete("/api/v1/blog/1")
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(blog)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

    }

    /*
     * Test - PUT mapping "/api/v1/blog" to update a blog, by mocking service class
     */
    @Test
    public void givenBlogToUpdateThenShouldReturnUpdatedBlog() throws Exception {
        when(blogService.updateBlog(any())).thenReturn(blog);
        mockMvc.perform(put("/api/v1/blog").contentType(APPLICATION_JSON).content(asJsonString(blog)))
                .andExpect(status().isOk()).andDo(MockMvcResultHandlers.print());

    }

    /*
     * Test - GET mapping "/api/v1/blog/2" to get a blog by id, by mocking service class
     * throw BlogNotFoundException and Expect string "Blog not found with id: 2"
     */
    @Test
    public void givenBlogIdNotFoundThenShouldReturnNotFound() throws Exception {
        when(blogService.getBlogById(2)).thenReturn(null); // Simulate blog not found

        // Act & Assert: Blog not found
        mockMvc.perform(get("/api/v1/blog/2")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("Blog not found with id: 2", result.getResponse().getContentAsString()));
    }

    /*
     * Test - GET mapping "/api/v1/blog/1" to get a blog by id, by mocking service class
     * throw RuntimeException and Expect string "An error occurred: Some error"
     */
    @Test
    public void givenBlogServiceThrowsExceptionThenShouldReturnInternalServerError() throws Exception {
        when(blogService.saveBlog(any())).thenThrow(new BlogAlreadyExistsException("Blog with ID 1 already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/blog")
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(blog)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Blog with ID 1 already exists"))
                .andDo(MockMvcResultHandlers.print());
    }

    /*
     * Test - POST mapping "/api/v1/blog" to save a blog, by mocking service class
     * throw BlogAlreadyExistsException and Expect string "Blog with ID 1 already exists"
     */
    @Test
    public void givenBlogAlreadyExistsThenShouldReturnConflict() throws Exception {
        when(blogService.saveBlog(any())).thenThrow(new RuntimeException("Some error occurred"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/blog")
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(blog)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred: An error occurred: Some error occurred"))
                .andDo(MockMvcResultHandlers.print());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}