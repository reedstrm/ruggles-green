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

import javax.annotation.Nullable;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;

/**
 *  A high-level interface for displaying error pages.
 */
public class ErrorPages {
    private static final String ERROR_TEMPLATE = "org.cnx.web.error";
    private static final String NOT_FOUND_TEMPLATE = "org.cnx.web.notFound";
    private static final String GOTO_OLD_SITE_TEMPLATE = "org.cnx.web.gotoOldSite";

    private static final String ERROR_STATUS_CODE_PARAM = "statusCode";
    private static final String ERROR_TRACKING_NUMBER_PARAM = "trackingNumber";
    private static final String ERROR_MESSAGE_PARAM = "message";
    private static final String ERROR_STACK_TRACE_PARAM = "stackTrace";

    private static final String GOTO_OLD_SITE_URL_PARAM = "redirectUrl";

    private final SoyTofu tofu;
    private final WebViewConfiguration config;

    @Inject
    public ErrorPages(@WebViewTemplate final SoyTofu tofu, final WebViewConfiguration config) {
        this.tofu = tofu;
        this.config = config;
    }

    /**
     *  Render a generic error page.
     *
     *  @param statusCode The HTTP status being returned
     *  @param trackingNumber A tracking number to present to the user when reporting the error
     *  @param message Some additional information
     *  @param throwable The error that occurred
     *  @return The HTML for the error page
     */
    public String renderError(final int statusCode, @Nullable final String trackingNumber,
            @Nullable final String message, @Nullable final Throwable throwable) {
        final SoyMapData params = new SoyMapData(ERROR_STATUS_CODE_PARAM, statusCode);
        if (trackingNumber != null) {
            params.put(ERROR_TRACKING_NUMBER_PARAM, trackingNumber);
        }
        if (message != null) {
            params.put(ERROR_MESSAGE_PARAM, message);
        }
        if (throwable != null && config.isStackEnabled()) {
            params.put(ERROR_STACK_TRACE_PARAM, Throwables.getStackTraceAsString(throwable));
        }
        return tofu.render(ERROR_TEMPLATE, params, null);
    }

    /**
     *  Render a 404 page.
     *
     *  @return The HTML for the error page
     */
    public String render404() {
        final SoyMapData params = new SoyMapData();
        return tofu.render(NOT_FOUND_TEMPLATE, params, null);
    }

    /**
     *  Render a 404 page, with a suggestion to try the old site.
     *
     *  @param redirectUrl The URL on the old site to present to the user
     *  @return The HTML for the error page
     */
    public String render404OldSite(final String redirectUrl) {
        final SoyMapData params = new SoyMapData(GOTO_OLD_SITE_URL_PARAM, redirectUrl);
        return tofu.render(GOTO_OLD_SITE_TEMPLATE, params, null);
    }
}
