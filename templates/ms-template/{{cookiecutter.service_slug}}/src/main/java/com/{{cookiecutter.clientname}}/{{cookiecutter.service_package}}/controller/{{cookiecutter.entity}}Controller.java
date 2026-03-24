package com.

{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.controller;

import com.{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.domain.request.{{cookiecutter.entity}}Request;
import com.{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.domain.response.{{cookiecutter.entity}}Response;
import com.{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.service.{{cookiecutter.entity}}Service;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/{{ cookiecutter.entity.lower() }}")
public class {{cookiecutter.entity }}

Controller {

    @Autowired
    private {
        {
            cookiecutter.entity
        }
    } Service {
        {
            cookiecutter.entity.lower()
        }
    } Service;

    @PostMapping
    public ResponseEntity < {{cookiecutter.entity}} Response > create( @Valid @RequestBody {
        {
            cookiecutter.entity
        }
    } Request {
        {
            cookiecutter.entity.lower()
        }
    } Request){
        {
            {
                cookiecutter.entity
            }
        } Response response = this. {
            {
                cookiecutter.entity.lower()
            }
        } Service.create({{cookiecutter.entity.lower()}}Request);
        return ResponseEntity.accepted().body(response);
    }

}
