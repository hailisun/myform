package com.haili.myform.controller;

import com.haili.myform.helper.DisableAutowireRequireInitializer;
import com.haili.myform.helper.SpringApplicationContext;
import com.haili.myform.helper.TestJpaConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Objects.isNull;

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
public abstract class IntegrationTestBase<T> {

    @Autowired
    private ApplicationContext applicationContext;

    private static boolean shouldInject = true;

    @Autowired
    private SpringApplicationContext springApplicationContext;

    /**
     * you can use inject by @Inject
     * for example:
     *
     * @Override
     * @Inject({ SubSalesTypeController.class,
     * SubSalesTypeService.class
     * })
     * protected void inject() {}
     */
    protected abstract void inject();

    @Autowired
    @Qualifier("mock")
    private Object[] mockBeans;

    @BeforeEach
    public void setUpAll() {
        injectAllClasses();
        injectObjectOfTest();
        Optional.ofNullable(getControllerClassType()).ifPresent((controllerClass) -> {
            StandaloneMockMvcBuilder mvcBuilder = MockMvcBuilders.standaloneSetup(applicationContext.getBean(controllerClass));
            addInterceptorsOf(mvcBuilder);
            RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();
            RestAssuredMockMvc.mockMvc(mvcBuilder.build());
        });
        restMockito();
    }

    private void restMockito() {
        if (mockBeans != null) {
            Mockito.reset(mockBeans);
        }
    }

    private Class<T> getControllerClassType() {
        Type parameterizedType = getParameterizedType();
        if (!(parameterizedType instanceof ParameterizedType)) {
            return null;
        }
        return (Class<T>) ((ParameterizedType) parameterizedType).getActualTypeArguments()[0];
    }

    private Type getParameterizedType() {
        Class tempClass = this.getClass();
        while (!IntegrationTestBase.class.equals(tempClass.getSuperclass())) {
            tempClass = tempClass.getSuperclass();
        }
        return tempClass.getGenericSuperclass();
    }

    private void injectAllClasses() {
        if (!shouldInject) {
            return;
        }
        springApplicationContext.setApplicationContext(applicationContext);
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        defaultListableBeanFactory.clearMetadataCache();
        String[] postProcessorNames = defaultListableBeanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
        ConfigurationClassPostProcessor processor = (ConfigurationClassPostProcessor) defaultListableBeanFactory.getBean(postProcessorNames[0], BeanDefinitionRegistryPostProcessor.class);
        Map<String, InjectionMetadata> injectionMetadataCache = getInjectionMetadataCache();
        getClasses().forEach(c -> {
            if (c == null) {
                return;
            }
            BeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClassName(c.getName());
            defaultListableBeanFactory.registerBeanDefinition(c.getName(), beanDefinition);
            injectionMetadataCache.remove(c.getName());
        });
        processor.processConfigBeanDefinitions(defaultListableBeanFactory);
        shouldInject = false;
    }

    private Map getInjectionMetadataCache() {
        try {
            Field field = AutowiredAnnotationBeanPostProcessor.class.getDeclaredField("injectionMetadataCache");
            field.setAccessible(true);
            return (Map) field.get(SpringApplicationContext.getBean(AutowiredAnnotationBeanPostProcessor.class));
        } catch (Exception e) {
            log.warn("can not find declared field: injectionMetadataCache");
        }
        return new HashMap();
    }

    private void injectObjectOfTest() {
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = this.getClass();
        while (tempClass != null) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }
        for (Field field : fieldList) {
            try {
                field.setAccessible(true);
                field.set(this, applicationContext.getBean(field.getType()));
            } catch (Exception e) {
                log.warn("set value fail:" + field.getName());
            }
        }
    }

//    private List<Class> getClasses() {
//        ArrayList<Class> classList = new ArrayList<>();
//        Class tempClass = this.getClass();
//        while (tempClass != null && tempClass != IntegrationTestBase.class) {
//            try {
//                classList.addAll(Arrays.asList(tempClass.getDeclaredMethod("inject")
//                        .getAnnotation(Inject.class)
//                        .value()));
//                tempClass = tempClass.getSuperclass();
//            } catch (Exception e) {
//                tempClass = tempClass.getSuperclass();
//                log.warn("can not such method...");
//            }
//        }
//        classList.add(getControllerClassType());
//        return classList;
//    }

    protected MockMvcRequestSpecification given() {
        return RestAssuredMockMvc
                .given()
                .header("Accept", ContentType.JSON.withCharset("UTF-8"))
                .header("Content-Type", ContentType.JSON.withCharset("UTF-8"));
    }

//    private void addInterceptorsOf(StandaloneMockMvcBuilder standaloneMockMvcBuilder) {
//        standaloneMockMvcBuilder
//                .setControllerAdvice(new ControllerAdvice())
//                .setCustomArgumentResolvers(new JwtUserResolver());
//    }

    @AfterAll
    static void shouldInject() {
        shouldInject = true;
    }


}
