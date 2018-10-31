package com.ymmihw.spring.rest.docs;

import static java.util.Collections.singletonList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringRestDocsApplication.class)
@WebAppConfiguration
public class ApiDocumentationIntegrationTest {

  @Rule
  public final JUnitRestDocumentation restDocumentation =
      new JUnitRestDocumentation("target/generated-snippets");

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ObjectMapper objectMapper;

  private RestDocumentationResultHandler document;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    this.document = document("{method-name}", preprocessRequest(prettyPrint()),
        preprocessResponse(prettyPrint()));
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
        .apply(documentationConfiguration(this.restDocumentation)).alwaysDo(document).build();
  }

  @Test
  public void headersExample() throws Exception {
    this.document.document(responseHeaders(headerWithName("Content-Type")
        .description("The Content-Type of the payload, e.g. `application/hal+json`")));
    this.mockMvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  public void indexExample() throws Exception {
    this.mockMvc.perform(get("/")).andExpect(status().isOk())
        .andDo(document("index", links(linkWithRel("crud").description("The CRUD resource")),
            responseFields(subsectionWithPath("_links").description("Links to other resources")),
            responseHeaders(
                headerWithName("Content-Type").description("The Content-Type of the payload"))));
  }

  @Test
  public void crudGetExample() throws Exception {

    Map<String, String> tag = new HashMap<>();
    tag.put("name", "GET");

    String tagLocation = this.mockMvc
        .perform(get("/crud").contentType(MediaTypes.HAL_JSON)
            .content(this.objectMapper.writeValueAsString(tag)))
        .andExpect(status().isOk()).andReturn().getResponse().getHeader("Location");

    Map<String, Object> crud = new HashMap<>();
    crud.put("title", "Sample Model");
    crud.put("body", "http://www.baeldung.com/");
    crud.put("tags", singletonList(tagLocation));

    this.mockMvc.perform(get("/crud").contentType(MediaTypes.HAL_JSON)
        .content(this.objectMapper.writeValueAsString(crud))).andExpect(status().isOk());
  }

  @Test
  public void crudCreateExample() throws Exception {
    Map<String, Object> crud = new HashMap<>();
    crud.put("title", "Sample Model");
    crud.put("body", "http://www.baeldung.com/");
    this.mockMvc
        .perform(post("/crud")
            .contentType(MediaTypes.HAL_JSON).content(this.objectMapper.writeValueAsString(crud)))
        .andExpect(status().isCreated())
        .andDo(document("crud-create-example",
            requestFields(fieldWithPath("title").description("The title of the input"),
                fieldWithPath("body").description("The body of the input"))));
  }

  @Test
  public void crudDeleteExample() throws Exception {
    this.mockMvc.perform(delete("/crud/{id}", 10).contentType(MediaTypes.HAL_JSON))
        .andExpect(status().isOk()).andDo(document("crud-delete-example",
            pathParameters(parameterWithName("id").description("The id of the input to delete"))));
  }

  @Test
  public void crudPatchExample() throws Exception {

    Map<String, String> tag = new HashMap<>();
    tag.put("name", "PATCH");

    String tagLocation = this.mockMvc
        .perform(patch("/crud/10").contentType(MediaTypes.HAL_JSON)
            .content(this.objectMapper.writeValueAsString(tag)))
        .andExpect(status().isNoContent()).andReturn().getResponse().getHeader("Location");

    Map<String, Object> crud = new HashMap<>();
    crud.put("title", "Sample Model");
    crud.put("body", "http://www.baeldung.com/");
    crud.put("tags", singletonList(tagLocation));

    this.mockMvc.perform(patch("/crud/10").contentType(MediaTypes.HAL_JSON)
        .content(this.objectMapper.writeValueAsString(crud))).andExpect(status().isNoContent());
  }

  @Test
  public void crudPutExample() throws Exception {
    Map<String, String> tag = new HashMap<>();
    tag.put("name", "PUT");

    String tagLocation = this.mockMvc
        .perform(put("/crud/10").contentType(MediaTypes.HAL_JSON)
            .content(this.objectMapper.writeValueAsString(tag)))
        .andExpect(status().isAccepted()).andReturn().getResponse().getHeader("Location");

    Map<String, Object> crud = new HashMap<>();
    crud.put("title", "Sample Model");
    crud.put("body", "http://www.baeldung.com/");
    crud.put("tags", singletonList(tagLocation));

    this.mockMvc.perform(put("/crud/10").contentType(MediaTypes.HAL_JSON)
        .content(this.objectMapper.writeValueAsString(crud))).andExpect(status().isAccepted());
  }

}
