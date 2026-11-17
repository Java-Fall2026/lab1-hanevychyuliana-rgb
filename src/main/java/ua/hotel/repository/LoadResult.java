package ua.hotel.repository;

import java.util.List;

public record LoadResult(
        int readCount,
        int addedCount,
        List<String> rejectedRecords
) {
}
