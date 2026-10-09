import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager {
    private final Path dataFolder = Path.of("data");
    private final Path tasksFile = dataFolder.resolve("tasks.txt");
    private final Path reportFile = dataFolder.resolve("report.txt");

    public FileManager() throws IOException {
        Files.createDirectories(dataFolder);
    }

    public List<StudyTask> loadTasks() throws IOException {
        List<StudyTask> loadedTasks = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();

        if (!Files.exists(tasksFile)) {
            return loadedTasks;
        }

        int lineNumber = 0;
        try (BufferedReader reader =
                     Files.newBufferedReader(tasksFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = line.split("\\|", -1);
                    if (fields.length != 7) {
                        throw new IllegalArgumentException("Expected 7 fields.");
                    }

                    int id = Integer.parseInt(fields[0]);
                    String type = fields[1];
                    String title = fields[2];
                    String subject = fields[3];
                    String priority = fields[4];
                    int minutes = Integer.parseInt(fields[5]);
                    TaskStatus status = TaskStatus.valueOf(fields[6]);

                    if (id <= 0 || !ids.add(id)) {
                        throw new IllegalArgumentException("ID must be positive and unique.");
                    }
                    if (title.trim().isEmpty() || subject.trim().isEmpty()) {
                        throw new IllegalArgumentException("Title and subject cannot be blank.");
                    }
                    if (!priority.equals("Low") && !priority.equals("Medium")
                            && !priority.equals("High")) {
                        throw new IllegalArgumentException("Invalid priority.");
                    }
                    if (minutes < 1 || minutes > 1440) {
                        throw new IllegalArgumentException("Minutes must be from 1 to 1440.");
                    }

                    StudyTask task;
                    if (type.equals("Reading")) {
                        task = new ReadingTask(id, title, subject, priority, minutes, status);
                    } else if (type.equals("Coding")) {
                        task = new CodingTask(id, title, subject, priority, minutes, status);
                    } else {
                        throw new IllegalArgumentException("Invalid task type.");
                    }
                    loadedTasks.add(task);
                } catch (RuntimeException e) {
                    throw new IOException("Invalid data in " + tasksFile + " at line "
                            + lineNumber + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Invalid data")) {
                throw e;
            }
            throw new IOException("Could not read " + tasksFile + ": " + e.getMessage(), e);
        }

        return loadedTasks;
    }

    public void saveTasks(List<StudyTask> tasks) throws IOException {
        List<String> lines = new ArrayList<>();

        for (StudyTask task : tasks) {
            validateText(task.getTitle(), "title");
            validateText(task.getSubject(), "subject");
            lines.add(task.getId() + "|" + task.getTaskType() + "|"
                    + task.getTitle() + "|" + task.getSubject() + "|"
                    + task.getPriority() + "|" + task.getEstimatedMinutes() + "|"
                    + task.getStatus());
        }

        writeLinesSafely(tasksFile, lines);
    }

    public void saveReport(List<String> reportLines) throws IOException {
        writeLinesSafely(reportFile, reportLines);
    }

    private void validateText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException(fieldName + " cannot be blank.");
        }
        if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
            throw new IOException(fieldName + " cannot contain | or line breaks.");
        }
    }

    private void writeLinesSafely(Path target, List<String> lines) throws IOException {
        Path temporary = dataFolder.resolve(target.getFileName().toString() + ".tmp");

        try {
            try (BufferedWriter writer =
                         Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
            	
            }
            throw new IOException("Could not save " + target + ": " + e.getMessage(), e);
        }
    }
}
