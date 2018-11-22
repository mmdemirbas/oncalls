package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static com.github.mmdemirbas.oncalls.Utils.unmodifiableCopyOf;

/**
 * A {@link Timeline} implementation which applies patches on top of a base timeline.
 * <p>
 * This class is immutable, if the generic types {@link C} and {@link V} are immutable.
 */
public final class PatchedTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V> {
    private final Timeline<C, V>                            baseTimeline;
    private final List<Timeline<C, UnaryOperator<List<V>>>> patchTimelines;

    public PatchedTimeline(Timeline<C, V> baseTimeline, List<Timeline<C, UnaryOperator<List<V>>>> patchTimelines) {
        this.baseTimeline = baseTimeline;
        this.patchTimelines = unmodifiableCopyOf(patchTimelines);
    }

    @Override
    public TimelineSegment<C, V> toTimelineSegment(Range<? extends C> calculationRange) {
        return baseTimeline.mergeWith(patchTimelines,
                                      calculationRange,
                                      (values, patches) -> reduce((List<V>) new ArrayList<>(values),
                                                                  patches,
                                                                  (acc, patch) -> patch.apply(acc)));
    }
}