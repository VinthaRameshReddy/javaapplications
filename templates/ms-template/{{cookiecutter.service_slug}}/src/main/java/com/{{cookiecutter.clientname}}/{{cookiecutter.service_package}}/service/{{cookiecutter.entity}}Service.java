package com.

{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.service;

        {%if cookiecutter.with_db=="y"-%}
import com.{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.domain.entity.{{cookiecutter.entity}};
        {%endif-%}
import com.{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.domain.request.{{cookiecutter.entity}}Request;
import com.{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.domain.response.{{cookiecutter.entity}}Response;
        {%if cookiecutter.with_db=="y"-%}
import com.{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.repository.{{cookiecutter.entity}}Repository;
        {%endif-%}
        {%if cookiecutter.with_db=="y"-%}
import org.springframework.beans.factory.annotation.Autowired;
{%endif-%}
import org.springframework.stereotype.Service;

@Service
public class {{cookiecutter.entity }}

Service {
    {%if cookiecutter.with_db == "y" - %
    }
    @Autowired
    private {
        {
            cookiecutter.entity
        }
    } Repository {
        {
            cookiecutter.entity.lower()
        }
    } Repository;
    {%-endif %
    }
    public {
        {
            cookiecutter.entity
        }
    } Response create ({{cookiecutter.entity}} Request request){
        {%if cookiecutter.with_db == "y" - %
        }
        var saved = this. {
            {
                cookiecutter.entity.lower()
            }
        } Repository.save(new {
            {
                cookiecutter.entity
            }
        } (request.title(), request.isActive()));
        return new {
            {
                cookiecutter.entity
            }
        } Response(saved.getId(), saved.getTitle(), saved.isActive());
        {%elif cookiecutter.with_db == "n" - %
        }
        return new {
            {
                cookiecutter.entity
            }
        } Response("id", request.title(), request.isActive());
        {%endif %
        }
    }
}
