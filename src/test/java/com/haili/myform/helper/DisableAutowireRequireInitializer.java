package com.haili.myform.helper;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;

public class DisableAutowireRequireInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        GenericApplicationContext ctx = (GenericApplicationContext) applicationContext;
        ctx.registerBeanDefinition(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
                                   BeanDefinitionBuilder
                                       .rootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class)
                                       .addPropertyValue("requiredParameterValue", false)
                                       .getBeanDefinition());
    }

}
