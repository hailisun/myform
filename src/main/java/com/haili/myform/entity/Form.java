package com.haili.myform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Entity
@Table(name = "form")
@NoArgsConstructor
@AllArgsConstructor
public class Form {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "model_id")
    @NotNull(message = "modelId cannot be null")
    private int modelId;

    private Map<String, String> data;
}
