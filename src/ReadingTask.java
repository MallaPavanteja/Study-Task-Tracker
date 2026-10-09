public class ReadingTask extends StudyTask {
    public ReadingTask(int id, String title, String subject, String priority,
                       int estimatedMinutes, TaskStatus status) {
        super(id, title, subject, priority, estimatedMinutes, status);
    }

    @Override
    public String getTaskType() {
        return "Reading";
    }
}
