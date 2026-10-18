package com.digitalfix.rabbitadmin.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digitalfix.rabbitadmin.dto.QueueStatus;
import com.digitalfix.rabbitadmin.service.RabbitAdminService;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitMQController {

    private static final Set<String> VALID_EXCHANGE_TYPES = Set.of("direct", "topic", "fanout", "headers");

    private final RabbitAdminService adminService;

    public RabbitMQController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createQueue(@RequestParam(required = false) String queueName) {
        if (queueName == null || queueName.isBlank()) {
            return badRequest("queueName es obligatorio");
        }
        adminService.createQueue(queueName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Cola creada correctamente", "queueName", queueName));
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(
            @RequestParam(required = false) String exchangeName,
            @RequestParam(required = false) String type) {
        if (exchangeName == null || exchangeName.isBlank()) {
            return badRequest("exchangeName es obligatorio");
        }
        if (type == null || type.isBlank() || !VALID_EXCHANGE_TYPES.contains(type.toLowerCase())) {
            return badRequest("type es obligatorio y debe ser uno de: direct, topic, fanout, headers");
        }
        adminService.createExchange(exchangeName, type);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Exchange creado correctamente",
                        "exchangeName", exchangeName, "type", type.toLowerCase()));
    }

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, String>> createBinding(
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) String exchangeName,
            @RequestParam(required = false) String routingKey) {
        ResponseEntity<Map<String, String>> error = validateBindingParams(queueName, exchangeName, routingKey);
        if (error != null) {
            return error;
        }
        adminService.createBinding(queueName, exchangeName, routingKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Binding creado correctamente",
                        "queueName", queueName, "exchangeName", exchangeName, "routingKey", routingKey));
    }

    @GetMapping("/queues")
    public ResponseEntity<List<QueueStatus>> listQueues() {
        return ResponseEntity.ok(adminService.listDomainQueues());
    }

    @GetMapping("/queues/{queueName}")
    public ResponseEntity<?> getQueue(@PathVariable String queueName) {
        QueueStatus status = adminService.getQueueStatus(queueName);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Cola no encontrada: " + queueName));
        }
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/queues/{queueName}")
    public ResponseEntity<Map<String, String>> deleteQueue(@PathVariable String queueName) {
        adminService.deleteQueue(queueName);
        return ResponseEntity.ok(Map.of("mensaje", "Cola eliminada", "queueName", queueName));
    }

    @DeleteMapping("/exchanges/{exchangeName}")
    public ResponseEntity<Map<String, String>> deleteExchange(@PathVariable String exchangeName) {
        adminService.deleteExchange(exchangeName);
        return ResponseEntity.ok(Map.of("mensaje", "Exchange eliminado", "exchangeName", exchangeName));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Map<String, String>> deleteBinding(
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) String exchangeName,
            @RequestParam(required = false) String routingKey) {
        ResponseEntity<Map<String, String>> error = validateBindingParams(queueName, exchangeName, routingKey);
        if (error != null) {
            return error;
        }
        adminService.deleteBinding(queueName, exchangeName, routingKey);
        return ResponseEntity.ok(Map.of("mensaje", "Binding eliminado",
                "queueName", queueName, "exchangeName", exchangeName, "routingKey", routingKey));
    }

    @PostMapping("/queues/{queueName}/purge")
    public ResponseEntity<Map<String, String>> purgeQueue(@PathVariable String queueName) {
        adminService.purgeQueue(queueName);
        return ResponseEntity.ok(Map.of("mensaje", "Cola purgada", "queueName", queueName));
    }

    private ResponseEntity<Map<String, String>> validateBindingParams(
            String queueName, String exchangeName, String routingKey) {
        if (queueName == null || queueName.isBlank()) {
            return badRequest("queueName es obligatorio");
        }
        if (exchangeName == null || exchangeName.isBlank()) {
            return badRequest("exchangeName es obligatorio");
        }
        if (routingKey == null || routingKey.isBlank()) {
            return badRequest("routingKey es obligatorio");
        }
        return null;
    }

    private static ResponseEntity<Map<String, String>> badRequest(String mensaje) {
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }
}
