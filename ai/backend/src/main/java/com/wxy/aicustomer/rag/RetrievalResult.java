package com.wxy.aicustomer.rag;

import java.util.List;

/**
 * 一次知识库检索的结果：拼接好的上下文 + 结构化来源。
 */
public record RetrievalResult(List<SourceRef> sources, String contextText, boolean degraded) {

    public static RetrievalResult empty(boolean degraded) {
        return new RetrievalResult(List.of(), "", degraded);
    }

    public boolean hasContext() {
        return !sources.isEmpty() && !contextText.isBlank();
    }
}
