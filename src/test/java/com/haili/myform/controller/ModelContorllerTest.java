package com.haili.myform.controller;

import com.haili.myform.entity.Model;
import com.haili.myform.helper.DisableAutowireRequireInitializer;
import com.haili.myform.helper.Inject;
import com.haili.myform.helper.SpringApplicationContext;
import com.haili.myform.helper.TestJpaConfig;
import com.haili.myform.repository.ModelRepository;
import com.haili.myform.service.ModelService.ModelService;
import com.jayway.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        initializers = {DisableAutowireRequireInitializer.class},
        classes = {
                SpringApplicationContext.class,
                TestJpaConfig.class,
        },
        loader = AnnotationConfigContextLoader.class
)
@TestPropertySource(locations = {"classpath:application-test.yml"})
@Transactional
@Rollback
@Slf4j
@WebAppConfiguration
public class ModelContorllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testUserLogin() throws Exception {
//        given().accept()
        mockMvc.perform(get("/demo/test").accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().json("{'foo':'bar'}"));
    }

//    @Override
    @Inject({
            ModelContorller.class,
            ModelService.class,
    })
    protected void inject() {
    }

    @Mock
    private ModelRepository modelRepository;

    @Test
    void should_save_successfully_when_Model_data_is_ok(){
        Model Model = com.haili.myform.entity.Model.builder().desc("").build();

        given()
                .body(Model)
                .when()
                .post("/model")
                .then()
                .statusCode(HttpStatus.CREATED.value());

    }

}
