package com.haili.myform.service.ModelService;

import com.haili.myform.entity.Model;
import com.haili.myform.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelService {

    @Autowired
    private ModelRepository modelRepository;

    public Model save(Model model) {
        return modelRepository.save(model);
    }
}
