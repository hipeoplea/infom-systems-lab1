package ru.hipeoplea.is.lab1.util;

import java.util.function.Function;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;

/**
 * Utility for creating {@link Pageable} instances with common validation.
 */
public final class PageRequestFactory {
    private PageRequestFactory() {
    }

    public static Pageable build(
            int page,
            int pageSize,
            String sortBy,
            String sortDir,
            Function<String, String> sortSanitizer) {
        if (page < 1) {
            throw new BadRequestException("page must be >= 1, but got " + page);
        }
        if (pageSize < 1) {
            throw new BadRequestException("pageSize must be >= 1, but got " + pageSize);
        }
        int zeroBasedPage = Math.max(0, page - 1);
        Sort.Direction direction =
                "desc".equalsIgnoreCase(sortDir)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortSanitizer.apply(sortBy));
        return PageRequest.of(zeroBasedPage, pageSize, sort);
    }
}

