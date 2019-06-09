package com.haili.myform.controller;

import com.haili.myform.entity.Model;
import com.haili.myform.service.ModelService.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ModelContorller {

    @Autowired
    private ModelService modelService;

    @PostMapping("/model")
    public Model create(@RequestBody Model model, BindingResult result){
        validate(result);
        return modelService.save(model);
    }

    private void validate(BindingResult result) {
        if(result.hasFieldErrors()){
            List<FieldError> errors = result.getFieldErrors();
            errors.stream().forEach(item -> Assert.isTrue(false, item.getDefaultMessage()));
        }
    }
}
