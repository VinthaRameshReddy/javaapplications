package com.

{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.domain.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "{{ cookiecutter.entity.lower() }}")
public class {{cookiecutter.entity }}extends

AuditAwareBaseEntity {

    private String title;
    private boolean isActive;

    public {
        {
            cookiecutter.entity
        }
    } () {
    }

    public {
        {
            cookiecutter.entity
        }
    } (String title,boolean isActive){
        this.title = title;
        this.isActive = isActive;
    }

    public String getTitle () {
        return title;
    }

    public boolean isActive () {
        return isActive;
    }
}
