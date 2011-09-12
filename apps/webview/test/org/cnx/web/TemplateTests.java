/*
 * Copyright (C) 2011 The CNX Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.cnx.web;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableMap;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;

import org.junit.Test;

public class TemplateTests {
    private static class MockConfiguration extends WebViewConfiguration {
        private final String analyticsCode;
        private final String siteMessage;
        private final String feedbackLink;

        public MockConfiguration(String analyticsCode, String siteMessage, String feedbackLink) {
            this.analyticsCode = analyticsCode;
            this.siteMessage = siteMessage;
            this.feedbackLink = feedbackLink;
        }

        @Override
        public String getFeedbackLink() {
            return feedbackLink;
        }

        @Override
        public String getAnalyticsCode() {
            return analyticsCode;
        }

        @Override
        public String getSiteMessage() {
            return siteMessage;
        }
    }

    private SoyTofu buildTofu(final WebViewConfiguration config) {
        final WebViewModule module = new WebViewModule();
        return module.provideTofu(config, new SoyFileSet.Builder());
    }

    @Test
    public void analyticsCodeShouldBeInserted() throws Exception {
        final String code =
                "_gaq.push(['_setAccount', 'UA-123456-4']);\n"
                + "_gaq.push(['_setDomainName', '.cnx.org']);\n"
                + "_gaq.push(['_trackPageview']);\n"
                + "_gaq.push(['abcdef._setAccount', 'UA-5555555-1']);\n"
                + "_gaq.push(['abcdef._setDomainName', '.cnx.org']);\n"
                + "_gaq.push(['abcdef._trackPageview']);\n";
        final SoyTofu tofu = buildTofu(new MockConfiguration(code, "", ""));
        final SoyMapData params = new SoyMapData(
                "title", "Hello",
                "contentHtml", "Hello, World!");
        assertTrue(tofu.render("org.cnx.web.page", params, null).contains(code));
    }

    @Test
    public void feedbackLinkShouldBeInserted() throws Exception {
        final String link = "https://docs.google.com/spreadsheet/embeddedform?formkey=dEpWYXc1aGh4dG56SEh0dWZlSldSR0E6MQ";
        final SoyTofu tofu = buildTofu(new MockConfiguration("", "", link));
        final SoyMapData params = new SoyMapData(
                "title", "Hello",
                "contentHtml", "Hello, World!");
        assertTrue(tofu.render("org.cnx.web.page", params, null).contains(
                "href=\"" + link + "\""));
    }
}
