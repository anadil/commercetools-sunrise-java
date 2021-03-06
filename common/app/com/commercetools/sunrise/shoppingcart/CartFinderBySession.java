package com.commercetools.sunrise.shoppingcart;

import com.commercetools.sunrise.hooks.RequestHookContext;
import com.commercetools.sunrise.hooks.requests.CartQueryHook;
import com.commercetools.sunrise.myaccount.CustomerSessionUtils;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartState;
import io.sphere.sdk.carts.queries.CartQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.queries.PagedResult;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static com.commercetools.sunrise.shoppingcart.CartSessionUtils.overwriteCartSessionData;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class CartFinderBySession implements CartFinder<Http.Session> {

    @Inject
    private SphereClient sphereClient;
    @Inject
    private HttpExecutionContext httpExecutionContext;
    @Inject
    private RequestHookContext hookContext;
    @Inject
    private MiniCartBeanFactory miniCartBeanFactory;

    @Override
    public CompletionStage<Optional<Cart>> findCart(final Http.Session session) {
        final CompletionStage<Optional<Cart>> cartStage = fetchCart(session);
        cartStage.thenAcceptAsync(cart ->
                overwriteCartSessionData(cart.orElse(null), session, miniCartBeanFactory), httpExecutionContext.current());
        return cartStage;
    }

    private CompletionStage<Optional<Cart>> fetchCart(final Http.Session session) {
        return buildQuery(session)
                .map(query -> CartQueryHook.runHook(hookContext, query))
                .map(query -> sphereClient.execute(query)
                        .thenApply(PagedResult::head))
                .orElseGet(() -> completedFuture(Optional.empty()));
    }

    private Optional<CartQuery> buildQuery(final Http.Session session) {
        return CustomerSessionUtils.getCustomerId(session)
                .map(this::buildQueryByCustomerId)
                .map(Optional::of)
                .orElseGet(() -> CartSessionUtils.getCartId(session)
                        .map(this::buildQueryByCartId))
                .map(query -> query
                        .plusPredicates(cart -> cart.cartState().is(CartState.ACTIVE))
                        .plusExpansionPaths(c -> c.shippingInfo().shippingMethod()) // TODO use run hook on cart query instead
                        .plusExpansionPaths(c -> c.paymentInfo().payments())
                        .withSort(cart -> cart.lastModifiedAt().sort().desc())
                        .withLimit(1));
    }

    private CartQuery buildQueryByCartId(final String cartId) {
        return CartQuery.of().plusPredicates(cart -> cart.id().is(cartId));
    }

    private CartQuery buildQueryByCustomerId(final String customerId) {
        return CartQuery.of().plusPredicates(cart -> cart.customerId().is(customerId));
    }
}
