package com.marhababik360.catalog.service;

import com.marhababik360.catalog.dto.ServiceRequest;
import com.marhababik360.catalog.dto.ServiceResponse;
import com.marhababik360.catalog.dto.UserContext;
import com.marhababik360.catalog.exception.ApiException;
import com.marhababik360.catalog.mapper.ServiceMapper;
import com.marhababik360.catalog.model.Service;
import com.marhababik360.catalog.repository.ServiceRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServiceCatalogService {
    private final ServiceRepository serviceRepository;
    public ServiceCatalogService(ServiceRepository serviceRepository) { this.serviceRepository = serviceRepository; }

    public List<ServiceResponse> list(Map<String, String> query) {
        return serviceRepository.findAll(query.get("category"), query.get("workerId")).stream().map(ServiceMapper::toResponse).toList();
    }

    public ServiceResponse get(String id) {
        return ServiceMapper.toResponse(serviceRepository.findById(id).orElseThrow(() -> new ApiException(404, "Not Found", "Service not found")));
    }

    public ServiceResponse create(UserContext context, ServiceRequest request) {
        requireWorker(context);
        validate(request);
        String now = Instant.now().toString();
        Service service = new Service();
        service.id = UUID.randomUUID().toString();
        apply(service, request);
        service.workerId = context.userId;
        service.workerName = isBlank(context.fullName) ? "Worker" : context.fullName;
        service.createdAt = now;
        service.updatedAt = now;
        return ServiceMapper.toResponse(serviceRepository.save(service));
    }

    public ServiceResponse update(UserContext context, String id, ServiceRequest request) {
        requireWorker(context);
        validate(request);
        Service service = serviceRepository.findById(id).orElseThrow(() -> new ApiException(404, "Not Found", "Service not found"));
        if (!context.userId.equals(service.workerId)) throw new ApiException(403, "Forbidden", "Only the owner worker can update this service");
        apply(service, request);
        service.updatedAt = Instant.now().toString();
        return ServiceMapper.toResponse(serviceRepository.update(service));
    }

    public void delete(UserContext context, String id) {
        requireWorker(context);
        Service service = serviceRepository.findById(id).orElseThrow(() -> new ApiException(404, "Not Found", "Service not found"));
        if (!context.userId.equals(service.workerId)) throw new ApiException(403, "Forbidden", "Only the owner worker can delete this service");
        serviceRepository.delete(id);
    }

    private void apply(Service service, ServiceRequest request) {
        service.title = request.title.trim();
        service.category = request.category.trim();
        service.description = request.description.trim();
        service.price = request.price;
        service.images = request.images == null ? new ArrayList<>() : request.images;
        service.location = trimToNull(request.location);
    }

    private void validate(ServiceRequest request) {
        if (request == null) throw new ApiException(400, "Bad Request", "Request body is required");
        if (isBlank(request.title)) throw new ApiException(400, "Bad Request", "title is required");
        if (isBlank(request.category)) throw new ApiException(400, "Bad Request", "category is required");
        if (isBlank(request.description)) throw new ApiException(400, "Bad Request", "description is required");
        if (request.price == null || request.price < 0) throw new ApiException(400, "Bad Request", "price must be positive or zero");
    }

    private void requireWorker(UserContext context) {
        if (!"worker".equals(context.role)) throw new ApiException(403, "Forbidden", "Worker role is required");
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
    private String trimToNull(String value) { return isBlank(value) ? null : value.trim(); }
}
