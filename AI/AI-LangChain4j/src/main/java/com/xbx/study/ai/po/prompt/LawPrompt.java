package com.xbx.study.ai.po.prompt;

import dev.langchain4j.model.input.structured.StructuredPrompt;

@StructuredPrompt("根据中国{{legal}}法律, 解答一下问题：{{question}}")
public class LawPrompt {

    private String legal;
    private String question;

    public String getLegal() {
        return legal;
    }

    public void setLegal(String legal) {
        this.legal = legal;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
