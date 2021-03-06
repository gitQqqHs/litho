/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.litho;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.facebook.litho.animation.AnimatedProperties;
import com.facebook.litho.animation.DimensionValue;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ComponentsTestRunner.class)
public class TransitionTest {

  @Test
  public void testCollectRootBoundsTransitions() {
    Transition transition = Transition.create("rootKey").animate(AnimatedProperties.WIDTH);

    Transition.RootBoundsTransition rootWidthTransition = new Transition.RootBoundsTransition();
    TransitionUtils.collectRootBoundsTransitions(
        "rootKey", transition, AnimatedProperties.WIDTH, rootWidthTransition);

    assertThat(rootWidthTransition.hasTransition).isTrue();
    assertThat(rootWidthTransition.appearTransition).isNull();

    Transition.RootBoundsTransition rootHeightTransition = new Transition.RootBoundsTransition();
    TransitionUtils.collectRootBoundsTransitions(
        "rootKey", transition, AnimatedProperties.HEIGHT, rootHeightTransition);

    assertThat(rootHeightTransition.hasTransition).isFalse();
  }

  @Test
  public void testCollectRootBoundsTransitionsAppearComesAfterAllLayout() {
    Transition transition =
        Transition.parallel(
            Transition.allLayout(),
            Transition.create("rootKey").animate(AnimatedProperties.HEIGHT).appearFrom(10),
            Transition.create("otherkey").animate(AnimatedProperties.ALPHA));

    Transition.RootBoundsTransition rootHeightTransition = new Transition.RootBoundsTransition();
    TransitionUtils.collectRootBoundsTransitions(
        "rootKey", transition, AnimatedProperties.HEIGHT, rootHeightTransition);

    assertThat(rootHeightTransition.hasTransition).isTrue();
    assertThat(rootHeightTransition.appearTransition).isNotNull();
  }

  @Test
  public void testCollectRootBoundsTransitionsAppearComesBeforeAllLayout() {
    Transition transition =
        Transition.parallel(
            Transition.create("rootKey").animate(AnimatedProperties.HEIGHT).appearFrom(10),
            Transition.allLayout(),
            Transition.create("otherkey").animate(AnimatedProperties.ALPHA));

    Transition.RootBoundsTransition rootHeightTransition = new Transition.RootBoundsTransition();
    TransitionUtils.collectRootBoundsTransitions(
        "rootKey", transition, AnimatedProperties.HEIGHT, rootHeightTransition);

    assertThat(rootHeightTransition.hasTransition).isTrue();
    assertThat(rootHeightTransition.appearTransition).isNotNull();
  }

  @Test
  public void testCollectRootBoundsTransitionsExtractAppearFrom() {
    Transition transition =
        Transition.create("rootKey").animate(AnimatedProperties.HEIGHT).appearFrom(10);

    Transition.RootBoundsTransition rootHeightTransition = new Transition.RootBoundsTransition();
    TransitionUtils.collectRootBoundsTransitions(
        "rootKey", transition, AnimatedProperties.HEIGHT, rootHeightTransition);

    assertThat(rootHeightTransition.hasTransition).isTrue();
    assertThat(rootHeightTransition.appearTransition).isNotNull();

    LayoutState layoutState = mock(LayoutState.class);
    LayoutOutput rootLayout = new LayoutOutput();
    rootLayout.setBounds(0, 0, 300, 100);
    when(layoutState.getMountableOutputAt(0)).thenReturn(rootLayout);

    int animateFrom =
        (int)
            Transition.getRootAppearFromValue(
                rootHeightTransition.appearTransition, layoutState, AnimatedProperties.HEIGHT);

    assertThat(animateFrom).isEqualTo(10);
  }

  @Test
  public void testCollectRootBoundsTransitionsExtractAppearFromDimensionValue() {
    Transition transition =
        Transition.create("rootKey")
            .animate(AnimatedProperties.HEIGHT)
            .appearFrom(DimensionValue.heightPercentageOffset(50));

    Transition.RootBoundsTransition rootHeightTransition = new Transition.RootBoundsTransition();
    TransitionUtils.collectRootBoundsTransitions(
        "rootKey", transition, AnimatedProperties.HEIGHT, rootHeightTransition);

    assertThat(rootHeightTransition.hasTransition).isTrue();
    assertThat(rootHeightTransition.appearTransition).isNotNull();

    LayoutState layoutState = mock(LayoutState.class);
    LayoutOutput rootLayout = new LayoutOutput();
    rootLayout.setBounds(0, 0, 300, 100);
    when(layoutState.getMountableOutputAt(0)).thenReturn(rootLayout);

    int animateFrom =
        (int)
            Transition.getRootAppearFromValue(
                rootHeightTransition.appearTransition, layoutState, AnimatedProperties.HEIGHT);

    float expectedAppearFrom = rootLayout.getBounds().height() * 1.5f;
    assertThat(animateFrom).isEqualTo((int) expectedAppearFrom);
  }
}
