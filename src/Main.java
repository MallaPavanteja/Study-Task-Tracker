import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String[] priorities = {"Low", "Medium", "High"};

    public static void main(String[] args) {
        try {
            FileManager fileManager = new FileManager();
            TaskService service = new TaskService(fileManager);
            System.out.println("Study Task Tracker started.");

            boolean running = true;
            while (running) {
                showMenu();
                int choice = readInt("Choose an option: ", 0, 6);

                try {
                    switch (choice) {
                        case 1:
                            addTask(service, "Reading");
                            break;
                        case 2:
                            addTask(service, "Coding");
                            break;
                        case 3:
                            displayTasks(service.getAllTasks());
                            break;
                        case 4:
                            displayTasks(service.getPendingTasks());
                            break;
                        case 5:
                            completeTask(service);
                            break;
                        case 6:
                            showProgress(service);
                            break;
                        case 0:
                            running = false;
                            System.out.println("Goodbye!");
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }
                } catch (IOException e) {
                    System.out.println("File error: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Input error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Could not start the project: " + e.getMessage());
            System.out.println("Correct the file problem and restart the program.");
        }
    }

    private static void showMenu() {
        System.out.println();
        System.out.println("===== STUDY TASK MENU =====");
        System.out.println("1. Add reading task");
        System.out.println("2. Add coding task");
        System.out.println("3. View all tasks");
        System.out.println("4. View pending tasks");
        System.out.println("5. Mark task done");
        System.out.println("6. View progress report");
        System.out.println("0. Exit");
    }

    private static void addTask(TaskService service, String type) throws IOException {
        System.out.println();
        System.out.println("ADD " + type.toUpperCase() + " TASK");
        String title = readRequiredText("Title: ");
        String subject = readRequiredText("Subject: ");

        System.out.println("Priority:");
        System.out.println("1. Low");
        System.out.println("2. Medium");
        System.out.println("3. High");
        int priorityChoice = readInt("Choose priority: ", 1, 3);
        String priority = priorities[priorityChoice - 1];

        int minutes = readInt("Estimated minutes (1-1440): ", 1, 1440);
        StudyTask task = service.addTask(type, title, subject, priority, minutes);
        System.out.println("Task " + task.getId() + " saved. Status: PENDING");
    }

    private static void displayTasks(List<StudyTask> tasks) {
        System.out.println();
        if (tasks.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.printf("%-5s %-10s %-24s %-16s %-8s %-10s %-10s%n",
                "ID", "TYPE", "TITLE", "SUBJECT", "PRIORITY", "MINUTES", "STATUS");
        for (StudyTask task : tasks) {
            System.out.printf("%-5d %-10s %-24s %-16s %-8s %-10d %-10s%n",
                    task.getId(), task.getTaskType(), shorten(task.getTitle(), 23),
                    shorten(task.getSubject(), 15), task.getPriority(),
                    task.getEstimatedMinutes(), task.getStatus());
        }
    }

    private static void completeTask(TaskService service) throws IOException {
        int id = readInt("Task ID: ", 1, Integer.MAX_VALUE);
        System.out.println(service.markDone(id));
    }

    private static void showProgress(TaskService service) throws IOException {
        List<String> report = service.buildProgressReport();
        System.out.println();
        for (String line : report) {
            System.out.println(line);
        }

        String answer = readText("Save report? (Y/N): ");
        if (answer.equalsIgnoreCase("Y")) {
            service.saveProgressReport();
            System.out.println("Report saved to data/report.txt");
        }
    }

    private static String readRequiredText(String prompt) {
        while (true) {
            String value = readText(prompt).trim();
            if (!value.isEmpty()) {
                if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
                    System.out.println("Do not use | or line breaks.");
                } else {
                    return value;
                }
            } else {
                System.out.println("This field cannot be blank. Please try again.");
            }
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readText(prompt).trim();
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static String shorten(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
