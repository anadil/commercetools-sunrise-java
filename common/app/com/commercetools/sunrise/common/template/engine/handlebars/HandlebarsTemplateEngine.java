package com.commercetools.sunrise.common.template.engine.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.commercetools.sunrise.common.pages.PageData;
import com.commercetools.sunrise.common.template.cms.CmsService;
import com.commercetools.sunrise.common.template.engine.TemplateNotFoundException;
import com.commercetools.sunrise.common.template.engine.TemplateRenderException;
import com.commercetools.sunrise.common.template.engine.TemplateEngine;
import com.commercetools.sunrise.common.template.i18n.I18nResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

public final class HandlebarsTemplateEngine implements TemplateEngine {

    private static final Logger logger = LoggerFactory.getLogger(HandlebarsTemplateEngine.class);
    static final String LANGUAGE_TAGS_IN_CONTEXT_KEY = "app-language-tags";
    private final Handlebars handlebars;

    private HandlebarsTemplateEngine(final Handlebars handlebars) {
        this.handlebars = handlebars;
    }

    @Override
    public String render(final String templateName, final PageData pageData, final List<Locale> locales) {
        final Template template = compileTemplate(templateName);
        final Context context = createContext(pageData, locales);
        try {
            logger.debug("Rendering template " + templateName);
            return template.apply(context);
        } catch (IOException e) {
            throw new TemplateRenderException("Context could not be applied to template " + templateName, e);
        }
    }

    public static TemplateEngine of(final List<TemplateLoader> templateLoaders, final I18nResolver i18NResolver, final CmsService cmsService) {
        final TemplateLoader[] loaders = templateLoaders.toArray(new TemplateLoader[templateLoaders.size()]);
        final Handlebars handlebars = new Handlebars()
                .with(loaders)
                .with(new HighConcurrencyTemplateCache())
                .infiniteLoops(true);
        handlebars.registerHelper("i18n", new HandlebarsI18nHelper(i18NResolver));
        handlebars.registerHelper("cms", new HandlebarsCmsHelper(cmsService));
        handlebars.registerHelper("json", new HandlebarsJsonHelper<>());
        return new HandlebarsTemplateEngine(handlebars);
    }

    private Context createContext(final PageData pageData, final List<Locale> locales) {
        final Context context = Context.newBuilder(pageData)
                .resolver(
                        MapValueResolver.INSTANCE,
                        JavaBeanValueResolver.INSTANCE,
                        PlayJavaFormResolver.INSTANCE
                )
                .build();
        context.data(LANGUAGE_TAGS_IN_CONTEXT_KEY, locales.stream().map(Locale::toLanguageTag).collect(toList()));
        return context;
    }

    private Template compileTemplate(final String templateName) {
        try {
            return handlebars.compile(templateName);
        } catch (IOException e) {
            throw new TemplateNotFoundException("Could not find the default template", e);
        }
    }

}