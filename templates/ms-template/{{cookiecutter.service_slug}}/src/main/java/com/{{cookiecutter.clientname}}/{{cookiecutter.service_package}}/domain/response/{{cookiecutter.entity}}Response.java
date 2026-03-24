package com.{{cookiecutter.clientname}}.{{cookiecutter.service_package }}.domain.response;

public record {{cookiecutter.entity }}

Response(
        String id,
        String title,
        boolean isActive) {
}
