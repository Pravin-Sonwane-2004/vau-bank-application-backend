package com.vaul.vaul.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaul.vaul.entities.IdempotencyRecord;
import com.vaul.vaul.enums.idempotency.IdempotencyStatus;
import com.vaul.vaul.repositories.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(
            IdempotencyRecordRepository idempotencyRecordRepository,
            ObjectMapper objectMapper
    ) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> T execute(
            String operation,
            String idempotencyKey,
            Object requestBody,
            Class<T> responseType,
            Supplier<T> action
    ) {
        String normalizedKey = normalizeKey(idempotencyKey);
        String requestHash = hashRequest(requestBody);

        Optional<IdempotencyRecord> existingRecord = idempotencyRecordRepository
                .findByOperationAndIdempotencyKey(operation, normalizedKey);
        if (existingRecord.isPresent()) {
            return resolveExistingRecord(existingRecord.get(), requestHash, responseType);
        }

        IdempotencyRecord record = new IdempotencyRecord();
        record.setOperation(operation);
        record.setIdempotencyKey(normalizedKey);
        record.setRequestHash(requestHash);
        record.setStatus(IdempotencyStatus.PROCESSING);

        try {
            idempotencyRecordRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException exception) {
            IdempotencyRecord concurrentRecord = idempotencyRecordRepository
                    .findByOperationAndIdempotencyKey(operation, normalizedKey)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Request with this idempotency key already exists"
                    ));
            return resolveExistingRecord(concurrentRecord, requestHash, responseType);
        }

        T response = action.get();
        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setResponseBody(writeAsJson(response));
        idempotencyRecordRepository.save(record);
        return response;
    }

    private String normalizeKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }
        return idempotencyKey.trim();
    }

    private String hashRequest(Object requestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] payloadHash = digest.digest(writeAsJson(requestBody).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(payloadHash);
        } catch (NoSuchAlgorithmException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to hash request");
        }
    }

    private <T> T resolveExistingRecord(
            IdempotencyRecord record,
            String requestHash,
            Class<T> responseType
    ) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Idempotency key has already been used for a different request"
            );
        }

        if (record.getStatus() != IdempotencyStatus.COMPLETED || record.getResponseBody() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Request with the same idempotency key is already being processed"
            );
        }

        try {
            return objectMapper.readValue(record.getResponseBody(), responseType);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to restore idempotent response"
            );
        }
    }

    private String writeAsJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to serialize payload");
        }
    }
}
