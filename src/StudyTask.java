public abstract class StudyTask {
    private int id;
    private String title;
    private String subject;
    private String priority;
    private int estimatedMinutes;
    private TaskStatus status;

    public StudyTask(int id, String title, String subject, String priority,
                     int estimatedMinutes, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.priority = priority;
        this.estimatedMinutes = estimatedMinutes;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getPriority() {
        return priority;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void markDone() {
        status = TaskStatus.DONE;
    }

    public abstract String getTaskType();
}
