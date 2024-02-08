package io.github.mityavasilyev.zeebedevhelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final static String debugProcessName = "zeebe_dev_helper_process";

    private final static String create_instance_cmd_template = "zbctl --insecure create instance %s --variables '%s'";
    private final static String deploy_debug_process_cmd_template = "zbctl --insecure --address localhost:26500 deploy %s";
    private final File bpmnTemplate = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("process_template.bpmn")).getFile());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void triggerJobWorkerWithCustomVariables(String jobWorkerName, Map<String, Object> variables) throws IOException {
        var deployedProcess = deployDebugBpmnProcess(jobWorkerName);
        startInstanceWithCustomVariables(debugProcessName, variables);
    }

    private DebugProcess deployDebugBpmnProcess(String jobWorkerName) throws IOException {

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

        executeTerminalCommand(format(deploy_debug_process_cmd_template, debugBpmnFile.toAbsolutePath()));

        return new DebugProcess(debugBpmnFileName, debugBpmnFile, jobWorkerName);
    }

    private void startInstanceWithCustomVariables(String debugProcessName, Map<String, Object> variables) throws JsonProcessingException {

        String preparedCmd = format(create_instance_cmd_template, debugProcessName, objectMapper.writeValueAsString(variables));

        executeTerminalCommand(preparedCmd);
    }

    private void executeTerminalCommand(String command) {
        log.info(format("Executing command: %s", command));

        // Check if the system is Windows
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");

        // Use "cmd.exe" and "/c" for Windows, "bash" and "-c" for Unix systems
        String[] cmd = isWindows ? new String[]{"cmd.exe", "/c", command} : new String[]{"bash", "-c", command};

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
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public record DebugProcess(
            String fileName,
            Path pathToFile,
            String jobWorkerName
    ) {
    }

}
