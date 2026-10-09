public class CodingTask extends StudyTask {
    public CodingTask(int id, String title, String subject, String priority,
                      int estimatedMinutes, TaskStatus status) {
        super(id, title, subject, priority, estimatedMinutes, status);
    }

    @Override
    public String getTaskType() {
        return "Coding";
    }
}
