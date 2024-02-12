package io.github.mityavasilyev.zeebedevhelper.controller;

import io.github.mityavasilyev.zeebedevhelper.dto.AbstractResponse;
import io.github.mityavasilyev.zeebedevhelper.dto.ErrorResponse;
import io.github.mityavasilyev.zeebedevhelper.dto.SuccessResponse;
import io.github.mityavasilyev.zeebedevhelper.service.ZbctlExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class HelperController {

    private final ZbctlExecutorService executorService;

    @PostMapping("/{jobWorkerName}")
    public ResponseEntity<AbstractResponse<Object>> triggerJobWorker(@PathVariable String jobWorkerName, @RequestBody Map<String, Object> variables) {
        try {
            executorService.triggerJobWorkerWithCustomVariables(jobWorkerName, variables);
            return ResponseEntity.ok(new SuccessResponse<>("Successfully triggered " + jobWorkerName));
        } catch (Exception exception) {
            return ResponseEntity
                    .status(500)
                    .body(new ErrorResponse<>(Map.of("jobWorkerName", jobWorkerName, "contextVariables", variables), exception));
        }
    }
}
