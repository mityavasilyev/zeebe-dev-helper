package io.github.mityavasilyev.zeebedevhelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class HelperController {

    private final ZbctlExecutorService executorService;

    @PostMapping("/{jobWorkerName}")
    public ResponseEntity<SuccessResponse<Object>> triggerJobWorker(@PathVariable String jobWorkerName, @RequestBody Map<String, Object> variables) throws IOException {
        executorService.triggerJobWorkerWithCustomVariables(jobWorkerName, variables);
        return ResponseEntity.ok(new SuccessResponse<>("Successfully triggered " + jobWorkerName));
    }
}
