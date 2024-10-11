package com.spring.example.util;

import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

public class EntityMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public static <T, U> U mapToRequest(T entity, Class<U> requestClass) {
        return modelMapper.map(entity, requestClass);
    }

    public static <T, U> U mapToResponse(T entity, Class<U> responseClass) {
        return modelMapper.map(entity, responseClass);
    }

    public static <T, U> U mapToEntity(T request, Class<U> entityClass) {
        return modelMapper.map(request, entityClass);
    }

    public static <T, U> List<U> mapToEntityList(List<T> requestList, Class<U> entityClass) {
        return requestList.stream().map(request -> modelMapper.map(request, entityClass)).collect(Collectors.toList());
    }
}
