package com.makaut.campussense.ai;

/**
 * Response payload returned by the Ask AI endpoint indicating either a live Gemini reply
 * or a graceful local-fallback signal.
 */
public class AiChatResponse {

    private String reply;
    private boolean groundedInSyllabus;
    private String sourceMode;

    public AiChatResponse() {
    }

    public AiChatResponse(String reply, boolean groundedInSyllabus, String sourceMode) {
        this.reply = reply;
        this.groundedInSyllabus = groundedInSyllabus;
        this.sourceMode = sourceMode;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isGroundedInSyllabus() {
        return groundedInSyllabus;
    }

    public void setGroundedInSyllabus(boolean groundedInSyllabus) {
        this.groundedInSyllabus = groundedInSyllabus;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }
}

