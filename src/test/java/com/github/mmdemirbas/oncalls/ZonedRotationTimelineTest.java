package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.mmdemirbas.oncalls.TestUtils.mapOf;
import static com.github.mmdemirbas.oncalls.TestUtils.pair;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ZonedRotationTimelineTest {
    @Test
    void immutability() {
        List<String> recipients = new ArrayList<>(asList("0", "1", "2"));
        ZonedRotationTimeline<String> timeline = new ZonedRotationTimeline<>(range(0, 100),
                                                                             iteration(10, subrange(1, 2)),
                                                                             recipients);

        assertEquals("0", timeline.recipientAtIndex(0));
        assertEquals("1", timeline.recipientAtIndex(1));
        assertEquals("2", timeline.recipientAtIndex(2));

        recipients.clear();

        assertEquals("0", timeline.recipientAtIndex(0));
        assertEquals("1", timeline.recipientAtIndex(1));
        assertEquals("2", timeline.recipientAtIndex(2));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void toTimeline_SimplestCase() {
        assertTimeline(range(0, 50), iteration(10, subrange(0, 10)),
                       range(0, 50),
                       mapOf(pair(zonedDateTime(0), asList(0)),
                             pair(zonedDateTime(10), asList(1)),
                             pair(zonedDateTime(20), asList(2)),
                             pair(zonedDateTime(30), asList(3)),
                             pair(zonedDateTime(40), asList(4)),
                             pair(zonedDateTime(50), asList())));
    }

    @Test
    void toTimeline_RecurrenceFinishedBeforeIteration() {
        assertTimeline(range(0, 45), iteration(10, subrange(0, 10)),
                       range(0, 45),
                       mapOf(pair(zonedDateTime(0), asList(0)),
                             pair(zonedDateTime(10), asList(1)),
                             pair(zonedDateTime(20), asList(2)),
                             pair(zonedDateTime(30), asList(3)),
                             pair(zonedDateTime(40), asList(4)),
                             pair(zonedDateTime(45), asList())));
    }

    @Test
    void toTimeline_CalculationRangeBeforeRecurrenceRange() {
        assertTimeline(range(20, 40), iteration(10, subrange(0, 10)), range(0, 20), mapOf());
    }

    @Test
    void toTimeline_RecurrenceRangeBeforeCalculationRange() {
        assertTimeline(range(0, 20), iteration(10, subrange(0, 10)), range(20, 40), mapOf());
    }

    @Test
    void toTimeline_CalculationRangeContainsRecurrenceRange() {
        assertTimeline(range(10, 30), iteration(10, subrange(0, 10)),
                       range(0, 50),
                       mapOf(pair(zonedDateTime(10), asList(0)),
                             pair(zonedDateTime(20), asList(1)),
                             pair(zonedDateTime(30), asList())));
    }

    @Test
    void toTimeline_RecurrenceRangeContainsCalculationRange() {
        assertTimeline(range(0, 50), iteration(10, subrange(0, 10)),
                       range(10, 30),
                       mapOf(pair(zonedDateTime(10), asList(1)),
                             pair(zonedDateTime(20), asList(2)),
                             pair(zonedDateTime(30), asList())));
    }

    @Test
    void toTimeline_CalculationRangeContainsIncompleteIterations() {
        assertTimeline(range(0, 50), iteration(10, subrange(0, 10)),
                       range(15, 35),
                       mapOf(pair(zonedDateTime(15), asList(1)),
                             pair(zonedDateTime(20), asList(2)),
                             pair(zonedDateTime(30), asList(3)),
                             pair(zonedDateTime(35), asList())));
    }

    @Test
    void toTimeline_SingleSubRange() {
        assertTimeline(range(0, 30), iteration(10, subrange(3, 7)),
                       range(0, 30),
                       mapOf(pair(zonedDateTime(3), asList(0)),
                             pair(zonedDateTime(7), asList()),
                             pair(zonedDateTime(13), asList(1)),
                             pair(zonedDateTime(17), asList()),
                             pair(zonedDateTime(23), asList(2)),
                             pair(zonedDateTime(27), asList())));
    }

    @Test
    void toTimeline_MultipleSubRanges() {
        assertTimeline(range(0, 30), iteration(10, subrange(3, 5), subrange(7, 9)),
                       range(0, 30),
                       mapOf(pair(zonedDateTime(3), asList(0)),
                             pair(zonedDateTime(5), asList()),
                             pair(zonedDateTime(7), asList(0)),
                             pair(zonedDateTime(9), asList()),
                             pair(zonedDateTime(13), asList(1)),
                             pair(zonedDateTime(15), asList()),
                             pair(zonedDateTime(17), asList(1)),
                             pair(zonedDateTime(19), asList()),
                             pair(zonedDateTime(23), asList(2)),
                             pair(zonedDateTime(25), asList()),
                             pair(zonedDateTime(27), asList(2)),
                             pair(zonedDateTime(29), asList())));
    }

    @Test
    void toTimeline_IncompleteIterationsAndMultipleSubRanges() {
        assertTimeline(range(0, 30), iteration(10, subrange(3, 5), subrange(7, 9)),
                       range(14, 28),
                       mapOf(pair(zonedDateTime(14), asList(1)),
                             pair(zonedDateTime(15), asList()),
                             pair(zonedDateTime(17), asList(1)),
                             pair(zonedDateTime(19), asList()),
                             pair(zonedDateTime(23), asList(2)),
                             pair(zonedDateTime(25), asList()),
                             pair(zonedDateTime(27), asList(2)),
                             pair(zonedDateTime(28), asList())));
    }

    private static void assertTimeline(Range<ZonedDateTime> recurrenceRange, Iterations<Instant> iteration,
                                       Range<ZonedDateTime> calculationRange,
                                       Map<ZonedDateTime, List<Integer>> expected) {
        List<Integer> participants = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Timeline<ZonedDateTime, Integer> recurrence = new ZonedRotationTimeline<>(recurrenceRange, iteration,
                                                                                  participants);
        TimelineSegment<ZonedDateTime, Integer> timeline = recurrence.toSegment(calculationRange);
        assertEquals(expected, timeline.toIntervalMap());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Range<ZonedDateTime> range(int startMillis, int endMillis) {
        return Range.of(zonedDateTime(startMillis), zonedDateTime(endMillis));
    }

    private static Range<Instant> subrange(int startMillis, int endMillis) {
        return Range.of(instant(startMillis), instant(endMillis));
    }

    private static ZonedDateTime zonedDateTime(int millis) {
        return ZonedDateTime.ofInstant(instant(millis), ZoneOffset.UTC);
    }

    private static Instant instant(int millis) {
        return Instant.ofEpochMilli(millis);
    }

    private static Iterations<Instant> iteration(int durationMillis, Range<Instant>... subranges) {
        return Iteration.of(instant(durationMillis), subranges).toIterations();
    }
}