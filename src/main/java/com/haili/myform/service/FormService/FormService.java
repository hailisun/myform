package com.haili.myform.service.FormService;

import com.haili.myform.entity.Form;
import com.haili.myform.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FormService {

    @Autowired
    private FormRepository formRepository;

    public Form save(Form form) {
        return formRepository.save(form);
    }

    public List<Form> getFormsByModelId(int modleId) {
        return formRepository.findAllById(Arrays.asList(modleId));
    }


}
