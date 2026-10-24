package pnnh.mcbilingual.translate;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for Ollama results. Keyed by kind + source text.
 */
public final class MachineTranslationCache {
    public enum Kind {
        WORD,
        SENTENCE,
        /** Cache key is a ROI id, not natural-language source text. */
        VISION
    }

    public enum Status {
        PENDING,
        OK,
        FAILED,
        TIMEOUT
    }

    public record Job(Kind kind, String source, Status status, String result, String error) {
        public Job {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(status, "status");
        }

        public static Job pending(Kind kind, String source) {
            return new Job(kind, source, Status.PENDING, null, null);
        }

        public static Job ok(Kind kind, String source, String result) {
            return new Job(kind, source, Status.OK, result, null);
        }

        public static Job failed(Kind kind, String source, String error) {
            return new Job(kind, source, Status.FAILED, null, error);
        }

        public static Job timeout(Kind kind, String source) {
            return new Job(kind, source, Status.TIMEOUT, null, null);
        }
    }

    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    public static String key(Kind kind, String source) {
        return kind.name() + '\0' + (source == null ? "" : source);
    }

    public Optional<Job> get(Kind kind, String source) {
        return Optional.ofNullable(jobs.get(key(kind, source)));
    }

    public Job put(Job job) {
        jobs.put(key(job.kind(), job.source()), job);
        return job;
    }

    public boolean putIfAbsentPending(Kind kind, String source) {
        return jobs.putIfAbsent(key(kind, source), Job.pending(kind, source)) == null;
    }

    public void clear() {
        jobs.clear();
    }
}
