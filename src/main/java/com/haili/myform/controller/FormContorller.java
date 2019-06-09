package com.haili.myform.controller;

import com.haili.myform.entity.Form;
import com.haili.myform.service.FormService.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FormContorller {

    @Autowired
    private FormService formService;

    @PostMapping("/form")
    public Form create(@RequestBody Form form){
        return formService.save(form);
    }

    @GetMapping("/form")
    public List<Form> get(int modleId){
        return formService.getFormsByModelId(modleId);
    }
}
