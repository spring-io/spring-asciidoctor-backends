/*
 * Copyright 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.asciidoctor.backend.integrationtest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import io.spring.asciidoctor.backend.testsupport.AsciidoctorExtension;
import io.spring.asciidoctor.backend.testsupport.ChromeContainer;
import io.spring.asciidoctor.backend.testsupport.ConvertedHtml;
import io.spring.asciidoctor.backend.testsupport.Source;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AssertDelegateTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for tab support.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(AsciidoctorExtension.class)
public class TabIntegrationTests {

	@Container
	private final ChromeContainer chrome = new ChromeContainer();

	@Test
	void singleTabIsCreated(@Source("singleTab.adoc") ConvertedHtml html) throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(1);
		assertThatTabs(listings.get(0)).hasSelected("Alpha").hasUnselected("Bravo")
				.hasDisplayedContentContaining("Alpha 1");
	}

	@Test
	void multipleTabsWithSameOptionsAreCreated(@Source("multipleTabsSameOptions.adoc") ConvertedHtml html)
			throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2).allSatisfy((element) -> assertThatTabs(element).hasSelected("Alpha")
				.hasUnselected("Bravo").hasDisplayedContentContaining("Alpha"));
	}

	@Test
	void multipleTabsWithDifferentOptionsAreCreated(@Source("multipleTabsDifferentOptions.adoc") ConvertedHtml html)
			throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2);
		assertThatTabs(listings.get(0)).hasSelected("Alpha").hasUnselected("Bravo")
				.hasDisplayedContentContaining("Alpha");
		assertThatTabs(listings.get(1)).hasSelected("Charlie").hasUnselected("Delta", "Echo")
				.hasDisplayedContentContaining("Charlie 1");
	}

	@Test
	void givenASingleTabWithCalloutsWhenUnselectedItemIsClickedThenItBecomesSelectedAndItsCalloutsBecomeVisible(
			@Source("singleSwitchWithCallouts.adoc") ConvertedHtml html) throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(1);
		assertThatTabs(listings.get(0)).hasSelected("Alpha").hasUnselected("Bravo")
				.hasDisplayedContentContaining("Alpha 1").hasDisplayedCalloutListContaining("Alpha callout")
				.uponClicking("Bravo").hasSelected("Bravo").hasUnselected("Alpha")
				.hasDisplayedCalloutListContaining("Bravo callout");
	}

	@Test
	void givenASingleTabWhenUnselectedItemIsClickedThenItBecomesSelected(@Source("singleTab.adoc") ConvertedHtml html)
			throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(1);
		assertThatTabs(listings.get(0)).hasSelected("Alpha").hasUnselected("Bravo")
				.hasDisplayedContentContaining("Alpha 1").uponClicking("Bravo").hasSelected("Bravo")
				.hasUnselected("Alpha").hasDisplayedContentContaining("Bravo 1");
	}

	@Test
	void givenMultipleTabsWithTheSameOptionsWhenUnselectedItemIsClickedThenItBecomesSelected(
			@Source("multipleTabsSameOptions.adoc") ConvertedHtml html) throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2);
		assertThatTabs(listings.get(1)).hasSelected("Alpha").hasUnselected("Bravo")
				.hasDisplayedContentContaining("Alpha 2");
		assertThatTabs(listings.get(0)).hasSelected("Alpha").hasUnselected("Bravo")
				.hasDisplayedContentContaining("Alpha 1").uponClicking("Bravo").hasSelected("Bravo")
				.hasUnselected("Alpha").hasDisplayedContentContaining("Bravo 1");
		assertThatTabs(listings.get(1)).hasSelected("Bravo").hasUnselected("Alpha")
				.hasDisplayedContentContaining("Bravo 2");
	}

	@Test
	void givenMultipleTabsWithDifferentOptionsWhenUnselectedItemIsClickedThenItBecomesSelected(
			@Source("multipleTabsDifferentOptions.adoc") ConvertedHtml html) throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2);
		assertThatTabs(listings.get(1)).hasSelected("Charlie").hasUnselected("Delta", "Echo")
				.hasDisplayedContentContaining("Charlie 1");
		assertThatTabs(listings.get(0)).hasSelected("Alpha").hasUnselected("Bravo")
				.hasDisplayedContentContaining("Alpha 1").uponClicking("Bravo").hasSelected("Bravo")
				.hasUnselected("Alpha").hasDisplayedContentContaining("Bravo 1");
		assertThatTabs(listings.get(1)).hasSelected("Charlie").hasUnselected("Delta", "Echo")
				.hasDisplayedContentContaining("Charlie 1");
	}

	@Test
	void secondaryBlockWithNoPrimary(@Source("secondaryBlockWithNoPrimary.adoc") ConvertedHtml html)
			throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome, (logs) -> {
			assertThat(logs).hasSize(1);
			assertThat(logs.iterator().next().getMessage())
					.endsWith("\"Found secondary block with no primary sibling\"");
		});
		List<WebElement> primaries = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(primaries).hasSize(0);
		List<WebElement> secondaries = driver.findElementsByCssSelector(".listingblock.secondary");
		assertThat(secondaries).hasSize(1);
	}

	@Test
	void tabsWithAndWithoutCallouts(@Source("tabsWithAndWithoutCallouts.adoc") ConvertedHtml html) throws IOException {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome);
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock");
		assertThat(listings).hasSize(3);
		assertThatTabs(listings.get(0)).hasNoCalloutList();
		assertThatTabs(listings.get(2)).hasDisplayedCalloutListContaining("Callout for tab one");
	}

	private static TabsAssert assertThatTabs(WebElement actual) {
		return assertThat(new TabsAssert(actual));
	}

	private static class TabsAssert extends AbstractAssert<TabsAssert, WebElement> implements AssertDelegateTarget {

		TabsAssert(WebElement actual) {
			super(actual, TabsAssert.class);
		}

		TabsAssert hasSelected(String item) {
			List<WebElement> selected = this.actual.findElements(By.cssSelector(".tab.selected"));
			assertThat(selected).extracting(WebElement::getText).containsExactly(item);
			return this;
		}

		TabsAssert hasUnselected(String... items) {
			List<WebElement> unselected = this.actual.findElements(By.cssSelector(".tab:not(.selected)"));
			assertThat(unselected).extracting(WebElement::getText).containsExactly(items);
			return this;
		}

		TabsAssert hasDisplayedContentContaining(String contained) {
			WebElement content = this.actual.findElement(By.cssSelector((".content:not(.hidden)")));
			assertThat(content.isDisplayed()).isTrue();
			assertThat(content.getText()).contains(contained);
			return this;
		}

		TabsAssert hasDisplayedCalloutListContaining(String contained) {
			WebElement content = this.actual.findElement(By.cssSelector(".content:not(.hidden)"))
					.findElement(By.cssSelector(".colist"));
			assertThat(content.isDisplayed()).isTrue();
			assertThat(content.getText()).contains(contained);
			return this;

		}

		TabsAssert hasNoCalloutList() {
			assertThat(this.actual.findElements(By.cssSelector(".colist"))).isEmpty();
			return this;
		}

		TabsAssert uponClicking(String item) {
			List<WebElement> unselected = this.actual.findElements(By.cssSelector(".tab:not(.selected)"));
			Optional<WebElement> match = unselected.stream().filter((element) -> element.getText().equals(item))
					.findFirst();
			assertThat(match).isPresent();
			match.get().click();
			return this;
		}

	}

}
