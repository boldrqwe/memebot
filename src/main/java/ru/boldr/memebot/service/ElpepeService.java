package ru.boldr.memebot.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class ElpepeService {
    public String runScript(String scriptPath, String videoFolderPath, String outputFolderPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, videoFolderPath, outputFolderPath);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = in.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = p.waitFor();
            if (exitCode == 0) {
                return output.toString();
            } else {
                // Обработка ошибки
                return "Error executing script. Exit code: " + exitCode;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Exception occurred: " + e.getMessage();
        }
    }
}
