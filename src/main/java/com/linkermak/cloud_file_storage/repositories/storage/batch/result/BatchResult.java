package com.linkermak.cloud_file_storage.repositories.storage.batch.result;

import java.util.List;

public record BatchResult(
        String operation,
        int requestedCount,
        int successCount,
        int failedCount,
        List<String> failedItems,
        List<String> errorMessages
) {
    public boolean partialSuccess() {
        return failedCount > 0 && successCount > 0;
    }

    public boolean hasErrors() {
        return failedCount > 0;
    }

    public String summaryForLog() {
        return "operation=" + operation
                + ", requestedCount=" + requestedCount
                + ", successCount=" + successCount
                + ", failedCount=" + failedCount
                + ", partialSuccess=" + partialSuccess();
    }

    public String detailsForLog() {
        return "failedItems=" + failedItems
                + ", errorMessages=" + errorMessages;
    }
}