package com.makaut.campussense.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for context-grounded Ask AI academic assistant queries.
 */
public class AiChatRequest {

    private String message;
    private String department;
    private String semester;
    private String academicContext;
    private List<ChatTurn> history = new ArrayList<>();

    public AiChatRequest() {
    }

    public AiChatRequest(String message, String department, String semester, String academicContext, List<ChatTurn> history) {
        this.message = message;
        this.department = department;
        this.semester = semester;
        this.academicContext = academicContext;
        this.history = history != null ? history : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getAcademicContext() {
        return academicContext;
    }

    public void setAcademicContext(String academicContext) {
        this.academicContext = academicContext;
    }

    public List<ChatTurn> getHistory() {
        return history;
    }

    public void setHistory(List<ChatTurn> history) {
        this.history = history != null ? history : new ArrayList<>();
    }

    /**
     * Single conversational turn in the Ask AI drawer rolling history.
     */
    public static class ChatTurn {
        private String role;
        private String text;

        public ChatTurn() {
        }

        public ChatTurn(String role, String text) {
            this.role = role;
            this.text = text;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}

