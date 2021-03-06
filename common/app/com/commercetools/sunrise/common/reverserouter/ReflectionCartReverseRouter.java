package com.commercetools.sunrise.common.reverserouter;

import com.commercetools.sunrise.common.pages.ParsedRoutes;
import play.mvc.Call;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
final class ReflectionCartReverseRouter extends ReflectionReverseRouterBase implements CartReverseRouter {

    private final ReverseCaller showCart;
    private final ReverseCaller processAddProductToCartForm;
    private final ReverseCaller processDeleteLineItemForm;
    private final ReverseCaller processChangeLineItemQuantityForm;

    @Inject
    private ReflectionCartReverseRouter(final ParsedRoutes parsedRoutes) {
        showCart = getCallerForRoute(parsedRoutes, "showCart");
        processAddProductToCartForm = getCallerForRoute(parsedRoutes, "processAddProductToCartForm");
        processDeleteLineItemForm = getCallerForRoute(parsedRoutes, "processDeleteLineItemForm");
        processChangeLineItemQuantityForm = getCallerForRoute(parsedRoutes, "processChangeLineItemQuantityForm");
    }
    @Override
    public Call showCart(final String languageTag) {
        return showCart.call(languageTag);
    }

    @Override
    public Call processAddProductToCartForm(final String languageTag) {
        return processAddProductToCartForm.call(languageTag);
    }

    @Override
    public Call processDeleteLineItemForm(final String languageTag) {
        return processDeleteLineItemForm.call(languageTag);
    }

    @Override
    public Call processChangeLineItemQuantityForm(final String languageTag) {
        return processChangeLineItemQuantityForm.call(languageTag);
    }
}
