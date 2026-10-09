import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TaskService {
    private final ArrayList<StudyTask> tasks = new ArrayList<>();
    private final FileManager fileManager;
    private int nextId = 1;

    public TaskService(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        tasks.addAll(fileManager.loadTasks());

        for (StudyTask task : tasks) {
            if (task.getId() >= nextId) {
                nextId = task.getId() + 1;
            }
        }
    }

    public StudyTask addTask(String type, String title, String subject,
                             String priority, int minutes) throws IOException {
        validateText(title, "Title");
        validateText(subject, "Subject");

        if (title.contains("|") || subject.contains("|")
                || title.contains("\n") || subject.contains("\n")
                || title.contains("\r") || subject.contains("\r")) {
            throw new IllegalArgumentException("Title and subject cannot contain | or line breaks.");
        }
        if (!priority.equals("Low") && !priority.equals("Medium")
                && !priority.equals("High")) {
            throw new IllegalArgumentException("Choose Low, Medium, or High priority.");
        }
        if (minutes < 1 || minutes > 1440) {
            throw new IllegalArgumentException("Minutes must be from 1 to 1440.");
        }

        StudyTask task;
        if (type.equalsIgnoreCase("Reading")) {
            task = new ReadingTask(nextId, title.trim(), subject.trim(), priority,
                    minutes, TaskStatus.PENDING);
        } else if (type.equalsIgnoreCase("Coding")) {
            task = new CodingTask(nextId, title.trim(), subject.trim(), priority,
                    minutes, TaskStatus.PENDING);
        } else {
            throw new IllegalArgumentException("Task type must be Reading or Coding.");
        }

        tasks.add(task);
        try {
            fileManager.saveTasks(tasks);
        } catch (IOException e) {
            tasks.remove(task);
            throw e;
        }

        nextId++;
        return task;
    }

    public List<StudyTask> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public List<StudyTask> getPendingTasks() {
        List<StudyTask> pending = new ArrayList<>();
        for (StudyTask task : tasks) {
            if (task.getStatus() == TaskStatus.PENDING) {
                pending.add(task);
            }
        }
        return pending;
    }

    public String markDone(int id) throws IOException {
        StudyTask selected = findById(id);
        if (selected == null) {
            return "No task found with ID " + id + ".";
        }
        if (selected.getStatus() == TaskStatus.DONE) {
            return "This task is already done.";
        }

        selected.markDone();
        try {
            fileManager.saveTasks(tasks);
        } catch (IOException e) {
            selected = findById(id);
            if (selected != null) {

                StudyTask restored;
                if (selected instanceof ReadingTask) {
                    restored = new ReadingTask(selected.getId(), selected.getTitle(),
                            selected.getSubject(), selected.getPriority(),
                            selected.getEstimatedMinutes(), TaskStatus.PENDING);
                } else {
                    restored = new CodingTask(selected.getId(), selected.getTitle(),
                            selected.getSubject(), selected.getPriority(),
                            selected.getEstimatedMinutes(), TaskStatus.PENDING);
                }
                int index = tasks.indexOf(selected);
                tasks.set(index, restored);
            }
            throw e;
        }
        return "Task saved. Status: DONE";
    }

    public List<String> buildProgressReport() {
        int[] counts = new int[2]; 
        long pendingMinutes = 0;
        long doneMinutes = 0;
        Set<String> subjects = new HashSet<>();

        for (StudyTask task : tasks) {
            subjects.add(task.getSubject().trim().toUpperCase(Locale.ROOT));
            if (task.getStatus() == TaskStatus.PENDING) {
                counts[0]++;
                pendingMinutes += task.getEstimatedMinutes();
            } else {
                counts[1]++;
                doneMinutes += task.getEstimatedMinutes();
            }
        }

        int total = counts[0] + counts[1];
        List<String> report = new ArrayList<>();
        report.add("STUDY TASK TRACKER - PROGRESS REPORT");
        report.add("------------------------------------");
        report.add("Total tasks: " + total);
        report.add("Pending tasks: " + counts[0]);
        report.add("Done tasks: " + counts[1]);
        report.add("Distinct subjects: " + subjects.size());
        report.add("Estimated minutes for pending tasks: " + pendingMinutes);
        report.add("Estimated minutes for done tasks: " + doneMinutes);

        if (total > 0) {
            double percentage = counts[1] * 100.0 / total;
            report.add(String.format(Locale.ROOT, "Completion: %.2f%%", percentage));
        } else {
            report.add("No tasks yet.");
        }

        return report;
    }

    public void saveProgressReport() throws IOException {
        fileManager.saveReport(buildProgressReport());
    }

    private StudyTask findById(int id) {
        for (StudyTask task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
    }
}
