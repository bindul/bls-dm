/*
 * Copyright (c) 2026. Bindul Bhowmik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.bindul.bls.dm.core.model.scoreorig;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.stream.Streams;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Wraps a set holding <code>ScoreLabel</code>s. Built to aid reuse.
 */
@EqualsAndHashCode @RequiredArgsConstructor
class ScoreLabelColl {

    private final ScoreComponent component;
    private Set<ScoreLabel> scoreLabels = null;
    
    public void addComputedScoreLabelIfNotPresent(ScoreLabel label) {
        Objects.requireNonNull(label);
        if (!containsScoreLabel(label)) {
            addComputedScoreLabel(label);
        }
    }
    
    public boolean addComputedScoreLabel(ScoreLabel label) {
        Objects.requireNonNull(label);
        ScoreLabel labelToAdd = label;
        if (!labelToAdd.isComputed()) {
            labelToAdd = labelToAdd.toBuilder().computed(true).build();
        }
        return addScoreLabel(labelToAdd);
    }
    
    public void addScoreLabelIfNotPresent(ScoreLabel label) {
        Objects.requireNonNull(label);
        if (!containsScoreLabel(label)) {
            addScoreLabel(label);
        }
    }
    
    public boolean addScoreLabel(ScoreLabel label) {
        Objects.requireNonNull(label);
        if (component != label.getComponent()) {
            throw new IllegalArgumentException("Can only add labels for : " + component.name());
        }
        if (null == this.scoreLabels) {
            this.scoreLabels = new HashSet<>();
        }
        return this.scoreLabels.add(label);
    }
    
    public boolean addAllScoreLabels(Collection<ScoreLabel> c) {
        Objects.requireNonNull(c);
        if (null == this.scoreLabels) {
            this.scoreLabels = new HashSet<>();
        }
        if(!c.stream().allMatch(c1 -> component == c1.getComponent())) {
            throw new IllegalArgumentException("Can only add labels for : " + component.name());
        }
        return this.scoreLabels.addAll(c);
    }
    
    public boolean containsScoreLabel(ScoreLabel label) {
        return null != this.scoreLabels && this.scoreLabels.contains(label);
    }

    public void clearScoreLabels() {
        if (null != this.scoreLabels) {
            this.scoreLabels.clear();
        }
    }
    
    public void clearCalculatedLabels () {
        if (null != this.scoreLabels) {
            this.scoreLabels.removeIf(ScoreLabel::isComputed);
        }
    }
    
    public Iterable<ScoreLabel> scoreLabelsIterable() {
        return FluentIterable.of(IterableUtils.emptyIfNull(this.scoreLabels));
    }
    
    public Stream<ScoreLabel> streamScoreLabels() {
        return Streams.of(this.scoreLabels);
    }
    
    public Set<ScoreLabel> getUnmodifiableScoreLabels() {
        return (null == this.scoreLabels) ? Collections.emptySet() : Collections.unmodifiableSet(this.scoreLabels);
    }
    
    public int getScoreLabelsCount() {
        return (null == this.scoreLabels) ? 0 : this.scoreLabels.size();
    }
}
