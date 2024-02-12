package io.github.mityavasilyev.zeebedevhelper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mityavasilyev.zeebedevhelper.exception.TerminalException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.OffsetDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZbctlExecutorService {

    @Value("${build.mode.docker.enabled}")
    private Boolean buildForDocker = true;

    @Value("${zeebe.host}")
    private String zeebeHost = "localhost";

    @Value("${zeebe.port}")
    private String zeebePort = "26500";

    private final static String DEBUG_PROCESS_NAME = "zeebe_dev_helper_process";
    private final File bpmnTemplate = buildForDocker
            ? loadTemplateFromDockerSourceFolder()
            : loadLocalTemplateFromResourcesFolder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DebugProcess triggerJobWorkerWithCustomVariables(String jobWorkerName, Map<String, Object> variables) throws IOException {
        var deployedProcess = createAndDeployDebugBpmnProcess(jobWorkerName);
        log.info("Deployed process " + deployedProcess.getFileName());

        startInstanceWithCustomVariables(DEBUG_PROCESS_NAME, variables, deployedProcess);
        log.info("Started new instance of debug process with variables: {}", variables);

        return deployedProcess;
    }

    private DebugProcess createAndDeployDebugBpmnProcess(String jobWorkerName) throws IOException {

        String debugBpmnFileName = format("%s-%s.bpmn", jobWorkerName, now());
        Path debugBpmnFileDestinationPath = Paths.get("src/main/resources", debugBpmnFileName);

        if (!Files.exists(debugBpmnFileDestinationPath.getParent()))
            Files.createDirectories(debugBpmnFileDestinationPath.getParent());
        Path debugBpmnFile = Files.copy(bpmnTemplate.toPath(), debugBpmnFileDestinationPath.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);

        // Replacing placeholder values with job worker name
        List<String> lines = Files.readAllLines(debugBpmnFile);
        lines = lines.stream()
                .map(line -> line.replace("placeholder_job_type", jobWorkerName)).collect(Collectors.toList());
        Files.write(debugBpmnFile, lines);

        executeTerminalCommand(format("zbctl --insecure --address %s:%s deploy %s", zeebeHost, zeebePort, debugBpmnFile.toAbsolutePath()));
        return DebugProcess.builder()
                .fileName(debugBpmnFileName)
                .pathToFile(debugBpmnFile)
                .jobWorkerName(jobWorkerName)
                .build();
    }

    private void startInstanceWithCustomVariables(String debugProcessName, Map<String, Object> variables, DebugProcess debugProcess) throws JsonProcessingException {

        String preparedCmd = format("zbctl --insecure create instance %s --variables '%s'", debugProcessName, objectMapper.writeValueAsString(variables));
        executeTerminalCommand(preparedCmd);
        debugProcess.setContextVariables(variables);    // To verify variables in case you need it elsewhere
    }

    private void executeTerminalCommand(String command) {
        log.info(format("Executing command: %s", command));

        // Check if the system is Windows
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");

        String[] cmd = isWindows ? new String[]{"cmd.exe", "/c", command} : new String[]{"sh", "-c", command};
        ProcessBuilder pb = new ProcessBuilder(cmd);

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            log.info("Execution result:\n" + sb.toString());
            int exitCode = process.waitFor();
            log.info("Finished execution with exit code: " + exitCode);
        } catch (IOException | InterruptedException exception) {
            log.error("Failed to execute command: {}", command);
            throw new TerminalException("Failed to execute command: %s".formatted(command), exception);
        }
    }

    private File loadLocalTemplateFromResourcesFolder() {
        try {
            return new File(Objects.requireNonNull(getClass().getClassLoader().getResource("process_template.bpmn")).getFile());
        } catch (NullPointerException exception) {
            log.info("No template file in resources folder");
            return null;
        }
    }

    private File loadTemplateFromDockerSourceFolder() {
        log.info("Using Docker Mode bpmn template location");
        return new File("/home/gradle/process_template.bpmn");
    }


    @Data
    @Builder
    public static class DebugProcess {
            private String fileName;
            private Path pathToFile;
            private String jobWorkerName;
            private Map<String, Object> contextVariables;
    }

}
