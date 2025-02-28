package org.jbehave.core.parsers;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Composite;
import org.jbehave.core.steps.StepType;
import org.junit.jupiter.api.Test;

class RegexCompositeParserBehaviour {

    private static final String NL = "\n";
    private static final String CRLF = "\r\n";

    private CompositeParser parser = new RegexCompositeParser();

    @Test
    void shouldParseEmptySteps() {
        List<Composite> composites = parser.parseComposites(EMPTY);
        assertThat(composites, equalTo(Collections.<Composite>emptyList()));
    }

    @Test
    void shouldParseCompositeStepWithEmptyComposedSteps() {
        String compositesAsText = "Composite: Given an empty composite step";
        List<Composite> composites = parser.parseComposites(compositesAsText);
        assertThat(composites.size(), equalTo(1));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "an empty composite step", 0, Collections.emptyList());
    }

    @Test
    void shouldParseCompositeStepWithEmptyComposedStepsEndingWithLineBreak() {
        String compositesAsText = "Composite: Given an empty composite step" + NL;
        List<Composite> composites = parser.parseComposites(compositesAsText);
        assertThat(composites.size(), equalTo(1));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "an empty composite step", 0, Collections.emptyList());
    }

    @Test
    void shouldParseCompositeStepWithEmptyComposedStepsEndingWithSpaceAndLineBreaks() {
        String compositesAsText = "Composite: Given an empty composite step "
                + NL + " "
                + NL;
        List<Composite> composites = parser.parseComposites(compositesAsText);
        assertThat(composites.size(), equalTo(1));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "an empty composite step", 0, Collections.emptyList());
    }

    @Test
    void shouldParseSingleCompositeStep() {
        String compositeStepsAsText = "Composite: Given a composite step"
                + NL + "Given a step"
                + NL + "Then another step";
        List<Composite> composites = parser.parseComposites(compositeStepsAsText);
        assertThat(composites.size(), equalTo(1));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "a composite step", 0,
                Arrays.asList("Given a step", "Then another step"));
    }

    @Test
    void shouldParseCompositeStepWithPriority() {
        String compositeStepsAsText = "Composite: Given a composite step"
                + NL + "Priority: 1"
                + NL + "Given a step"
                + NL + "Then another step";
        List<Composite> composites = parser.parseComposites(compositeStepsAsText);
        assertThat(composites.size(), equalTo(1));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "a composite step", 1,
                Arrays.asList("Given a step", "Then another step"));
    }

    @Test
    void shouldParseCompositeStepWithLineBreaks() {
        String compositeStepsAsText = "Composite: Given a composite step with UNIX separators"
                + NL + "Given a step"
                + NL + NL + "Then another step"
                + NL + "Composite: Given a composite step with MS-DOS separators"
                + CRLF + "Given a step"
                + CRLF + CRLF + "Then another step"
                + CRLF;
        List<Composite> composites = parser.parseComposites(compositeStepsAsText);
        assertThat(composites.size(), equalTo(2));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "a composite step with UNIX separators", 0,
                Arrays.asList("Given a step", "Then another step"));
        assertCompositeStep(composites.get(1), StepType.GIVEN, "a composite step with MS-DOS separators", 0,
                Arrays.asList("Given a step", "Then another step"));
    }

    @Test
    void shouldParseTwoCompositeStep() {
        String compositeStepsAsText = "Composite: Given the first composite step"
                + NL + "Given a step"
                + NL + "Composite: When the second composite step"
                + NL + "Then another step"
                + NL;
        List<Composite> composites = parser.parseComposites(compositeStepsAsText);
        assertThat(composites.size(), equalTo(2));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "the first composite step", 0,
                Collections.singletonList("Given a step"));
        assertCompositeStep(composites.get(1), StepType.WHEN, "the second composite step", 0,
                Collections.singletonList("Then another step"));
    }

    @Test
    void shouldParseTwoCompositeStepWithCRLF() {
        String compositeStepsAsText = "Composite: Given the first composite step"
                + CRLF + "Given a step"
                + CRLF + "Composite: When the second composite step"
                + CRLF + "Then another step"
                + CRLF;
        List<Composite> composites = parser.parseComposites(compositeStepsAsText);
        assertThat(composites.size(), equalTo(2));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "the first composite step", 0,
                Collections.singletonList("Given a step"));
        assertCompositeStep(composites.get(1), StepType.WHEN, "the second composite step", 0,
                Collections.singletonList("Then another step"));
    }

    @Test
    void shouldParseCompositeStepWithCustomLocale() {
        String compositeStepsAsText = "Композитный: Дано композитный шаг"
                + NL + "Дано шаг";
        CompositeParser localizedParser = new RegexCompositeParser(new LocalizedKeywords(new Locale("ru")));
        List<Composite> composites = localizedParser.parseComposites(compositeStepsAsText);
        assertThat(composites.size(), equalTo(1));
        assertCompositeStep(composites.get(0), StepType.GIVEN, "композитный шаг", 0,
                Collections.singletonList("Дано шаг"));
    }

    private void assertCompositeStep(Composite composite, StepType stepType, String stepWithoutStartingWord,
            int priority, List<String> steps) {
        assertThat(composite.getStepType(), equalTo(stepType));
        assertThat(composite.getStepWithoutStartingWord(), equalTo(stepWithoutStartingWord));
        assertThat(composite.getPriority(), equalTo(priority));
        assertThat(composite.getSteps(), equalTo(steps));
    }
}
