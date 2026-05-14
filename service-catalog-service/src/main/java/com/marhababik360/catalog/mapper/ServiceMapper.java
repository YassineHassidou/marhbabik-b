package com.marhababik360.catalog.mapper;

import com.marhababik360.catalog.dto.ServiceResponse;
import com.marhababik360.catalog.model.Service;

public final class ServiceMapper {
    private ServiceMapper() {}
    public static ServiceResponse toResponse(Service service) {
        ServiceResponse response = new ServiceResponse();
        response.id = service.id;
        response.title = service.title;
        response.category = service.category;
        response.description = service.description;
        response.price = service.price;
        response.images = service.images;
        response.location = service.location;
        response.workerId = service.workerId;
        response.workerName = service.workerName;
        response.createdAt = service.createdAt;
        response.updatedAt = service.updatedAt;
        return response;
    }
}
